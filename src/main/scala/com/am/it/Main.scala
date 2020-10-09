package com.am.it

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConverters.seqAsJavaListConverter


object Main extends App {
  KafkaTestUtil().withKafkaRunning {
    val record = new ProducerRecord[String, String]("topic", "value")
    val producer = new KafkaProducer(configs, new StringSerializer(), new StringSerializer())
    val meta = producer.send(record).get(timeout, TimeUnit.MILLISECONDS)
    println(s"record was sent with meta= ${(meta.hasOffset, meta.partition, meta.topic)}")

    val consumer = new KafkaConsumer(configs, new StringDeserializer(), new StringDeserializer())
      .tap(_.subscribe(List("topic").asJava))
    val res = consumer.poll(timeout)

    println(s"reading records resulted in ${res.iterator().toList.map(_.value())}")
    assert(res.iterator().next().value() == "value")
  }

  lazy val timeout = 30000

  lazy val configs = new Properties()
    .tap(_ put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9999"))
    .tap(_ put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "26214400"))
    .tap(_ put(ProducerConfig.ACKS_CONFIG, "all"))
    .tap(_ put(ConsumerConfig.GROUP_ID_CONFIG, "test"))
    .tap(_ put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "30000"))
    .tap(_ put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1"))
}
