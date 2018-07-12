package com.careercolony.postservices.notification

import akka.actor.{Actor, ActorRef}
import com.careercolony.postservices.factories.DatabaseAccess
import com.careercolony.postservices.model.{GetFriends, GetPost, Post}
import com.careercolony.postservices.notification.NotificationEvent._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class NotificationActor extends Actor with DatabaseAccess {

  var wsCount: Map[Int, ActorRef] = Map.empty[Int, ActorRef]
  var wsPost: Map[Int, ActorRef] = Map.empty[Int, ActorRef]

  override def receive: Receive = {
    case UserJoinedForCount(id, actorRef) =>
      wsCount += id -> actorRef
      getFriendsUnreadPost(id).onComplete {
        case Success(posts) =>
          actorRef ! NotificationCount(posts.size)
          println(s"Member ID $id connected into web socket!")
        case Failure(error) =>
          println(s"Member ID $id get post failed error: $error")
      }

    case UserJoinedForPost(id, actorRef) =>
      wsPost += id -> actorRef
      getFriendsUnreadPost(id).onComplete {
        case Success(posts) =>
          actorRef ! NotificationPost(posts)
          println(s"Member ID $id connected into web socket!")
        case Failure(error) =>
          println(s"Member ID $id get post failed error: $error")
      }

    case UserLeft(id) =>
      println(s"Member ID $id left connection!")
      wsCount -= id
      wsPost -= id

    case getPost: GetPost =>
      val friends: List[GetFriends] = getFriends(getPost.memberID)
      friends.foreach(frd => {
        wsCount.get(frd.memberID).foreach(_ ! NotificationCount(1))
        wsPost.get(frd.memberID).foreach(_ ! NotificationPost(List(getPost)))
      })

  }

}
