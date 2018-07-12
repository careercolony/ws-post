package com.careercolony.postservices.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.careercolony.postservices.factories.DatabaseAccess
import com.careercolony.postservices.notification.NotificationRoom


trait NotificationService extends DatabaseAccess {

  implicit val system: ActorSystem
  implicit val materializer: Materializer

  val notificationRoom: NotificationRoom

  def notification: Route =
    path("notification" / "memberID" / IntNumber / "count") { memberID =>
      println(s"received $memberID")
      handleWebsocketMessages(notificationRoom.webSocketFlowForCount(memberID))
    } ~ path("notification" / "memberID" / IntNumber / "posts") { memberID =>
      println(s"received $memberID")
      handleWebsocketMessages(notificationRoom.webSocketFlowForPost(memberID))
    }
}

