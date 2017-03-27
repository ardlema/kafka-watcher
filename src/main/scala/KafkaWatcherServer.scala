import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.ardlema.kafka.status.{KafkaStatusActor, KafkaStatusFlow, RouterActor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object KafkaWatcherServer extends App {

  implicit val system = ActorSystem("KafkaWatcher")
  implicit val materializer = ActorMaterializer()

  //TODO: Get this values from application.conf
  val interface = "localhost"
  val port = 8080
  val router = system.actorOf(Props[RouterActor], "router")
  val kakfaStatusActor = system.actorOf(Props(classOf[KafkaStatusActor], router, 2 seconds, 5 seconds))

  val route = get {
    pathPrefix("kafka-status") {
      handleWebSocketMessages(KafkaStatusFlow.graphFlowWithKafkaStatus(router))
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()
  binding.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  println("Server is down...")
}
