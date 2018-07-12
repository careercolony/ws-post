package com.careercolony.postservices.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.careercolony.postservices.notification.NotificationRoom

import scala.concurrent.{ExecutionContextExecutor, Future}

class StartNeo4jServer(implicit val system: ActorSystem,
                       implicit val materializer: ActorMaterializer,
                       implicit val executor: ExecutionContextExecutor)
  extends NotificationService with MongoService {

  val notificationRoom: NotificationRoom = new NotificationRoom(system)

  val routes: Route = notification ~ mongoService

  def startServer(address: String, port: Int): Future[Http.ServerBinding] = {
    Http().bindAndHandle(routes, address, port)
  }
}
