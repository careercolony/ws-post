package com.careercolony.postservices.routes

import akka.http.scaladsl.server.{Directives, Route}
import com.careercolony.postservices.factories.DatabaseAccess
import com.careercolony.postservices.model.{GetPost, Post}
import com.careercolony.postservices.notification.NotificationRoom
import com.careercolony.postservices.repo.JsonRepo
import spray.json._

import scala.util._

trait MongoService extends DatabaseAccess with Directives {

  val notificationRoom: NotificationRoom

  import JsonRepo._
  def mongoService: Route = path("get-all-post") {
    get {
      onComplete(retrieveAllPost) {
        case Success(result) => complete(result.toJson.toString())
        case Failure(error) => complete(s"Error: $error")
      }
    }
  } ~ path("get-post" / "memberID" / Segment) { memberID =>
    get {
      onComplete(retrievePost(memberID.toInt)) {
        case Success(result) => complete(result.toJson.toString())
        case Failure(error) => complete(s"Error: $error")
      }
    }
  } ~ path("new-post") {
    post {
      entity(as[Post]) { post =>
        onComplete(insertPost(post)) {
          case Success(result) =>
            notificationRoom.notificationActor ! result._2
            complete(s"Result : ${result._1.toString}")
          case Failure(error) => complete(s"Error: $error")
        }
      }
    }
  }

}