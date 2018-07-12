package com.careercolony.postservices.repo

import java.text.SimpleDateFormat

import com.careercolony.postservices.model.{Counter, GetPost}
import reactivemongo.bson._

import scala.util.Try

object BsonRepo {
  val format: SimpleDateFormat = new SimpleDateFormat("yyyy MM dd HH:mm:ss")

  implicit object GetPostReader extends BSONDocumentReader[GetPost] {
    def read(bson: BSONDocument): GetPost = {
      val post: Option[GetPost] = for {
        memberID <- bson.getAs[Int]("memberID")
        title <- bson.getAs[String]("title")
        description <- bson.getAs[String]("description")
        post_type <- bson.getAs[String]("post_type")
        author <- bson.getAs[String]("author")
        thumbnail_url <- bson.getAs[String]("thumbnail_url")
        post_url <- bson.getAs[String]("post_url")
        post_date <- bson.getAs[BSONDateTime]("post_date").map(x => format.format(new java.util.Date(x.value)))
        postID <-  bson.getAs[Int]("postID")
        readers <- Try(bson.getAs[List[String]]("readers")).toOption
      } yield GetPost(memberID.toInt, title, description, post_type, author, thumbnail_url, post_url, post_date, postID.toInt, readers)
      post.get
    }
  }

  implicit object CounterReader extends BSONDocumentReader[Counter] {
    def read(bson: BSONDocument): Counter = {
      for {
        _id <- bson.getAs[String]("_id")
        seq <- bson.getAs[Int]("seq")
      } yield Counter(_id, seq.toInt)
    }.get
  }

}
