package org.ardlema.kafka.status

import akka.actor.Actor
import akka.routing.{ AddRoutee, RemoveRoutee, Routee }

class RouterActor extends Actor {
  var routees = Set[Routee]()

  def receive = {
    case ar: AddRoutee    ⇒ routees = routees + ar.routee
    case rr: RemoveRoutee ⇒ routees = routees - rr.routee
    case msg              ⇒ routees.foreach(_.send(msg, sender))
  }
}
