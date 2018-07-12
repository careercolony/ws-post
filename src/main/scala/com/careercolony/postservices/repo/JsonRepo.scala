package com.careercolony.postservices.repo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.careercolony.postservices.model.{GetPost, Post}
import spray.json._

object JsonRepo extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val getPostFormats: RootJsonFormat[GetPost] = jsonFormat10(GetPost)
  implicit val postFormats: RootJsonFormat[Post] = jsonFormat8(Post)
}
