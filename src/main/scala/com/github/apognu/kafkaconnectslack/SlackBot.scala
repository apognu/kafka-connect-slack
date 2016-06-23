package com.github.apognu.kafkaconnectslack

import java.util.Collections

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import io.scalac.slack.{BotModules, MessageEventBus}
import io.scalac.slack.api.{APIKey, BotInfo, Start}
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.bots.system.{CommandsRecognizerBot, HelpBot}
import io.scalac.slack.common.{BaseMessage, OutboundMessage, Shutdownable}
import io.scalac.slack.common.actors.SlackBotActor
import io.scalac.slack.websockets.WebSocket
import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}
import org.apache.kafka.connect.source.SourceRecord

object SlackBot extends Shutdownable {

  val system = ActorSystem("ScalaKafkaBot")
  val eventBus = new MessageEventBus
  val botInfo: Option[BotInfo] = None

  var slackBot: Option[ActorRef] = None
  var callback: Option[(SourceRecord) => Unit] = None
  var topicName: String = ""

  class SlackBotBundle extends BotModules {
    override def registerModules(context: ActorContext, websocketClient: ActorRef) = {
      context.actorOf(Props(classOf[CommandsRecognizerBot], eventBus), "CommandProcessor")
      context.actorOf(Props(classOf[HelpBot], eventBus), "HelpBot")
      context.actorOf(Props(classOf[SlackBot], eventBus), "SlackMessageConnector")
    }
  }

  def start(apiKey: String, topic: String)(cb: (SourceRecord) => Unit) = {
    try {
      topicName = topic
      slackBot = Some(system.actorOf(Props(classOf[SlackBotActor], new SlackBotBundle(), eventBus, this, APIKey(apiKey), None), "SlackBot"))
      callback = Some(cb)

      slackBot.get ! Start

      system.awaitTermination()
    } catch {
      case e: Exception =>
        println(e.printStackTrace())

        system.shutdown()
        system.awaitTermination()
    }
  }

  override def shutdown() = {
    slackBot.get ! WebSocket.Release

    system.shutdown()
    system.awaitTermination()
  }

  class SlackBot(override val bus: MessageEventBus) extends AbstractBot {

    val schema = SchemaBuilder.struct().name("com.slack.message")
      .field("user", Schema.STRING_SCHEMA)
      .field("channel", Schema.STRING_SCHEMA)
      .field("text", Schema.STRING_SCHEMA)
      .build()

    override def help(channel: String): OutboundMessage =
      OutboundMessage(channel, "No help here, move along!")

    override def act: Receive = {
      case message: BaseMessage =>
        val record = new SourceRecord(
          Collections.singletonMap("channel", message.channel),
          Collections.singletonMap("position", System.currentTimeMillis()),
          topicName,
          schema,
          new Struct(schema)
            .put("user", message.user)
            .put("channel", message.channel)
            .put("text", message.text.stripLineEnd)
        )

        callback match {
          case Some(callback) => callback(record)
          case None =>
        }
    }

  }

}
