package com.careercolony.postservices

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.careercolony.postservices.routes.StartNeo4jServer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Boot extends App {
  implicit val system: ActorSystem = ActorSystem("Post-Akka-Service")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val server = new StartNeo4jServer()
  val config = server.config
  val serverUrl = config.getString("http.interface")
  val port = config.getInt("http.port")
  val binding = server.startServer(serverUrl, port)
  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => system.shutdown())
  println("Server is down...")
}