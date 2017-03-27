package org.ardlema.kafka.status

import java.util.Properties

import akka.actor.{ Actor, ActorRef }
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.json4s.jackson.Serialization
import scala.concurrent.duration.FiniteDuration

class KafkaStatusActor(router: ActorRef, delay: FiniteDuration, interval: FiniteDuration) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  context.system.scheduler.schedule(delay, interval) {
    implicit val formats = org.json4s.DefaultFormats
    router ! Serialization.write(getKafkaStatus)
  }

  val kakfaProperties = {
    val props = new Properties()
    props.put("bootstrap.servers", "127.0.0.1:9092")
    props.put("group.id", "kafka-status-group")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

  lazy val kafkaConsumer = new KafkaConsumer(kakfaProperties)

  override def receive: Actor.Receive = {
    case _ ⇒ // just ignore any messages
  }

  def getStats: Map[String, Long] = {

    val baseStats = Map[String, Long](
      "count.procs" → Runtime.getRuntime.availableProcessors(),
      "count.mem.free" → Runtime.getRuntime.freeMemory(),
      "count.mem.maxMemory" → Runtime.getRuntime.maxMemory(),
      "count.mem.totalMemory" → Runtime.getRuntime.totalMemory()
    )
    baseStats
  }

  def getKafkaStatus = {
    import scala.collection.JavaConversions._
    kafkaConsumer.listTopics().map(e ⇒ e._1 → e._2.size)
    //kafkaConsumer.listTopics.mapValues(_.toSet).map(e => ("topic" -> e._1))
  }
}
