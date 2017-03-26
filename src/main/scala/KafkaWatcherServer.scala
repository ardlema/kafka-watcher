import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext.Implicits.global

import scala.io.StdIn

object KafkaWatcherServer extends App {

  implicit val system = ActorSystem("KafkaWatcher")
  implicit val materializer = ActorMaterializer()

  //TODO: Get this values from application.conf
  val interface = "localhost"
  val port = 8080

  val route: Route = get {
    path("ws-echo") {
      handleWebSocketMessages(echoService)
    }
  }

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(text) ⇒ TextMessage("Echo: " + text)
    case _                        ⇒ TextMessage("Message type unsupported")

  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()
  binding.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  println("Server is down...")
}
