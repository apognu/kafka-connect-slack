package com.github.apognu.kafkaconnectslack

import java.util.{List => JList, Map => JMap}

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.{Importance, Type}
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceConnector

class SlackSourceConnector extends SourceConnector {

  var topic: String = ""
  var channelList: String = ""
  var apiKey: String = ""

  override def version(): String = "1.0"
  override val taskClass: Class[_ <: Task] = classOf[SlackSourceTask]

  override def start(conf: JMap[String, String]) = {
    topic = conf.getOrDefault(Config.kafkaTopicDirective, "")
    apiKey = conf.getOrDefault(Config.slackApiKeyDirective, "")

    if (Config.kafkaTopicDirective.isEmpty) throw new ConnectException(s"SlackSourceConnector configuration must include '${Config.kafkaTopicDirective}' setting")
    if (Config.slackApiKeyDirective.isEmpty) throw new ConnectException(s"SlackSourceConnector configuration must include '${Config.slackApiKeyDirective}' setting")
  }

  override def stop() = { }

  override def taskConfigs(maxTasks: Int): JList[JMap[String, String]] = {
    import scala.collection.JavaConverters._

    val configs = channelList.split(",").map { channel =>
      Map(
        Config.kafkaTopicDirective -> topic,
        Config.slackApiKeyDirective -> apiKey
      ).asJava
    }

    configs.toList.asJava
  }

  override def config(): ConfigDef = new ConfigDef()
    .define(Config.kafkaTopicDirective, Type.STRING, Importance.HIGH, "Destination Kafka topic")
    .define(Config.slackApiKeyDirective, Type.STRING, Importance.HIGH, "Slack API key")
}
