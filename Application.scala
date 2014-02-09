import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import akka.util._
import scala.concurrent._
import spray.can._
import spray.http._
import spray.routing._

object Application extends App {
  Database.fill()

  implicit val system = ActorSystem("reactivemongo-benchmark")

  val service = system.actorOf(Props[WebServiceActor], "webservice")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}

class WebServiceActor extends Actor with HttpService {
  import TestEntityJSONFormat._

  def actorRefFactory = context
  def receive = runRoute(route)

  implicit val timeout = Timeout(60000)
  implicit val executionContext = actorRefFactory.dispatcher

  val route =
    pathPrefix("db") {
      path("blocking") {
        complete {
          Await.result(Database.get(), duration.Duration.Inf).get
        }
      } ~
      path("nonblocking") {
        onSuccess(Database.get()) { e =>
          complete(e.get)
        }
      }
    }
}
