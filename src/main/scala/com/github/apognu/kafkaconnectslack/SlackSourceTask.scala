package com.github.apognu.kafkaconnectslack

import java.util.{LinkedList, List => JList, Map => JMap}
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.apache.kafka.connect.source.{SourceRecord, SourceTask}

import scala.concurrent.Future

class SlackSourceTask extends SourceTask {

  val queue: BlockingQueue[SourceRecord] = new LinkedBlockingQueue[SourceRecord]()

  override def version(): String = "1.0"

  override def start(props: JMap[String, String]) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    Future {
      SlackBot.start(props.get(Config.slackApiKeyDirective), props.get(Config.kafkaTopicDirective)) { record: SourceRecord =>
        queue.offer(record)
      }
    }
  }

  override def stop() = SlackBot.shutdown()

  sys.addShutdownHook(stop())

  override def poll: JList[SourceRecord] = {
    val result: JList[SourceRecord] = new LinkedList[SourceRecord]()

    if (queue.isEmpty) result.add(queue.take())
    queue.drainTo(result)

    result
  }
}