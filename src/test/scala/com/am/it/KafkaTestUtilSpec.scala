package com.am.it

import java.util.concurrent.TimeUnit
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.collection.JavaConversions.asScalaIterator

class KafkaTestUtilSpec extends AnyFlatSpec with should.Matchers {
  "withKafkaRunning" should "" in {
    KafkaTestUtil(kafkaPort = testKafkaPort).withKafkaRunning {
      // given
      val record = new ProducerRecord[String, String](topic, message)
      val producer = new KafkaProducer[String, String](configs)
      val consumer = new KafkaConsumer[String, String](configs).tap(_.subscribe(Collections.singleton(topic)))

      // when
      producer.send(record).get(timeout, TimeUnit.MILLISECONDS)
      val received = consumer.poll(timeout).iterator().toList.map(_.value())

      // then
      received shouldBe List(message)
    }

  }

  private lazy val message = "value"
  private lazy val topic = "topic"
  private lazy val timeout = 30000
  private lazy val testKafkaPort = 2000
  private lazy val configs = new Properties()
    .tap(_ put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s"localhost:$testKafkaPort"))
    .tap(_ put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "26214400"))
    .tap(_ put(ProducerConfig.ACKS_CONFIG, "all"))
    .tap(_ put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName))
    .tap(_ put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName))
    .tap(_ put(ConsumerConfig.GROUP_ID_CONFIG, "test"))
    .tap(_ put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "30000"))
    .tap(_ put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1"))
    .tap(_ put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName))
    .tap(_ put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName))

}
