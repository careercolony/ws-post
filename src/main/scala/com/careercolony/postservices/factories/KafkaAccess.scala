package com.careercolony.postservices.factories

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.collection.JavaConverters._

trait KafkaAccess {

  val config: Config = ConfigFactory.load("application.conf")

  implicit val kafkasys: ActorSystem = ActorSystem("Post-Akka-Service")
  implicit val kafkamaterializer: ActorMaterializer = ActorMaterializer.create(kafkasys)

  // Kafka configs
  val topic: String = config.getString("kafka.topic")
  val topic_1: String = config.getString("kafka.topic_1")
  val topic_2: String = config.getString("kafka.topic_2")
  val topic_3: String = config.getString("kafka.topic_3")
  val brokers: String = config.getString("kafka.brokers")
  val producerSettings: ProducerSettings[Array[Byte], String] =
    ProducerSettings(kafkasys, new ByteArraySerializer, new StringSerializer)
      .withBootstrapServers(brokers)
  val kafkaProducer: KafkaProducer[Array[Byte], String] =
    producerSettings.createKafkaProducer()

  def sendPostToKafka(post: String): Unit = {
    import java.util.Properties

    import org.apache.kafka.clients.producer._

    val props = new Properties()
    props.put("bootstrap.servers", brokers)

    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)
    val record: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, post)
    producer.send(record)
    producer.close()
  }

}
