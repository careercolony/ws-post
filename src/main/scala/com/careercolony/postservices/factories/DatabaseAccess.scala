package com.careercolony.postservices.factories

import com.careercolony.postservices.model._
import com.careercolony.postservices.repo._
import org.neo4j.driver.v1._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api._
import reactivemongo.bson._

import scala.concurrent.duration._
import scala.concurrent._
import scala.util.{Failure, Success, Try}

import spray.json._


trait DatabaseAccess extends KafkaAccess {

  import BsonRepo._
  import JsonRepo._

  // Neo4j configs
  val neo4jUrl: String = config.getString("neo4j.url")
  val userName: String = config.getString("neo4j.userName")
  val userPassword: String = config.getString("neo4j.userPassword")

  // Mongo configs
  // My settings (see available connection options)
  val mongoUri: String = config.getString("mongo.url")
  val username: String = config.getString("mongo.username")
  val password: String = config.getString("mongo.password")
  val database: String = config.getString("mongo.database")

  import ExecutionContext.Implicits.global // use any appropriate context

  // Connect to the database: Must be done only once per application
  val driver: MongoDriver = MongoDriver()
  val parsedUri: Try[MongoConnection.ParsedURI] = MongoConnection.parseURI(mongoUri)
  val connection: Try[MongoConnection] = parsedUri.map(driver.connection)

  // Database and collections: Get references
  val futureConnection: Future[MongoConnection] = Future.fromTry(connection)

  def db: Future[DefaultDB] = futureConnection.flatMap(_.database(database))

  val postCollection: Future[BSONCollection] = db.map(_[BSONCollection]("post"))

  def counterCollection: Future[BSONCollection] = db.map(_[BSONCollection]("counters"))


  def query1(memberID: Int): BSONDocument =
    BSONDocument("memberID" -> memberID)

  def query2(listOfMemberId: List[Int]): BSONDocument =
    BSONDocument("memberID" -> BSONDocument("$in" -> listOfMemberId))

  def retrievePost(memberIDs: Int): Future[List[GetPost]] =
    postCollection.flatMap(_.find(query1(memberIDs)).cursor[GetPost]().collect[List]())

  def retrievePosts(listOfMemberId: List[Int]): Future[List[GetPost]] =
    postCollection.flatMap(_.find(query2(listOfMemberId)).cursor[GetPost]().collect[List]())

  def retrieveAllPost: Future[List[GetPost]] =
    postCollection.flatMap(_.find(BSONDocument()).cursor[GetPost]().collect[List]())

  def getFriends(memberID: Int): List[GetFriends] = {
    val driver: Driver = GraphDatabase.driver(neo4jUrl, AuthTokens.basic(userName, userPassword))
    val session: Session = driver.session
    val script: String = s"MATCH (me { memberID: $memberID })-[rels:FRIEND*1..3]-(myfriend) WHERE ALL (r IN rels WHERE r.status = 'active') WITH COLLECT(myfriend) AS collected_friends UNWIND collected_friends AS activity  MATCH (p:feeds {memberID:activity.memberID}) WITH (p) AS e return e.memberID AS memberID"
    val result: StatementResult = session.run(script)

    var records: List[GetFriends] = List[GetFriends]()

    while (result.hasNext) {
      val record = result.next()
      records :+= GetFriends(record.get("memberID").asInt)
    }
    session.close()
    driver.close()
    records.distinct
  }

  def getFriendsUnreadPost(memberID: Int): Future[List[GetPost]] = {
    retrievePosts(getFriends(memberID).map(_.memberID)).mapTo[List[GetPost]]
      .map(_.filterNot(_.readers.exists(_.contains(memberID.toString))))
  }


  def getNextSequence(idKey: String): Int = {
    val f = counterCollection.flatMap(
      _.findAndUpdate(BSONDocument("_id" -> idKey), BSONDocument(
        "$inc" -> BSONDocument("seq" -> 1)
      ),
        fetchNewObject = true).map(_.result[Counter]))
    val result = Await.result(f, 5000 millis)

    val ret: Int = result match {
      case None => -1
      case Some(c: Counter) => c.seq
    }
    ret
  }

  def insertPost(p: Post): Future[(Int, GetPost)] = {

    val dateTime: BSONDateTime = BSONDateTime(System.currentTimeMillis)
    val postID: Int = getNextSequence("postID")
    val postDoc: BSONDocument = BSONDocument(
      "postID" -> postID,
      "memberID" -> p.memberID,
      "title" -> p.title,
      "description" -> p.description,
      "post_type" -> p.post_type,
      "author" -> p.author,
      "thumbnail_url" -> p.thumbnail_url,
      "post_url" -> p.post_url,
      "post_date" -> dateTime,
      "readers" -> p.readers
    )

    val getPost: GetPost =
      GetPost(p.memberID, p.title, p.description, p.post_type, p.author,
        p.thumbnail_url, p.post_url, format.format(new java.util.Date(dateTime.value)),
        postID, Some(p.readers))

    val inst: Future[Int] = postCollection.flatMap(_.insert(postDoc).map(_.n))

    inst.onComplete {
      case Success(result) =>
       println(s"Result post insert : $result")
       sendPostToKafka(p.toJson.toString)
      case Failure(error) =>
        println(s"Error insert post $error")
    }
    inst.map((_, getPost))
  }

}
