package org.ardlema.kafka.status

import java.io.File

import akka.actor.{Actor, ActorRef}
import play.api.libs.json.Json
import scala.concurrent.duration.FiniteDuration

class KafkaStatusActor(router: ActorRef, delay: FiniteDuration, interval: FiniteDuration) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  context.system.scheduler.schedule(delay, interval) {
    val json = Json.obj("stats" → getStats.map(el ⇒ el._1 → el._2))
    router ! Json.prettyPrint(json)
  }

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

    val roots = File.listRoots()
    val totalSpaceMap = roots.map(root ⇒ s"count.fs.total.${root.getAbsolutePath}" → root.getTotalSpace) toMap
    val freeSpaceMap = roots.map(root ⇒ s"count.fs.free.${root.getAbsolutePath}" → root.getFreeSpace) toMap
    val usuableSpaceMap = roots.map(root ⇒ s"count.fs.usuable.${root.getAbsolutePath}" → root.getUsableSpace) toMap

    baseStats ++ totalSpaceMap ++ freeSpaceMap ++ usuableSpaceMap
  }
}
