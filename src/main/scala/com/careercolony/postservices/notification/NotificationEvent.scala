package com.careercolony.postservices.notification

import akka.actor.ActorRef
import com.careercolony.postservices.model.GetPost

object NotificationEvent {

  sealed trait ChatEvent

  case class UserJoinedForCount(id: Int, userActor: ActorRef) extends ChatEvent

  case class UserJoinedForPost(id: Int, userActor: ActorRef) extends ChatEvent

  case class UserLeft(id: Int) extends ChatEvent

  case class IncomingMessage(text: String) extends ChatEvent

  case class NotificationCount(count: Int) extends ChatEvent {
    override def toString: String = "{\"count\": " + count.toString + "}"
  }

  case class NotificationPost(listOfPost: List[GetPost]) extends ChatEvent {
    override def toString: String = if(listOfPost.nonEmpty) "[" + listOfPost.map(_.toString).reduce(_+","+_) + "]" else "[]"
  }

  case class ChatMessage(sender: Int, text: String) extends ChatEvent

}
