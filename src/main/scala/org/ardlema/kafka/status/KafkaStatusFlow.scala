package org.ardlema.kafka.status

import akka.actor.{ ActorRef, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.scaladsl.{ Flow, Sink, Source }

object KafkaStatusFlow {

  def graphFlowWithKafkaStatus(router: ActorRef): Flow[Any, Message, _] = {

    val source = Source.actorPublisher[String](Props(classOf[KafkaStatusPublisher], router))
      .map[Message](x ⇒ TextMessage.Strict(x))

    Flow.fromSinkAndSource(Sink.ignore, source)
  }
}
