package com.am.it

import java.util.Properties

import kafka.server.KafkaServerStartable
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer

import scala.collection.JavaConverters.mapAsJavaMapConverter

/**
 * provides mechanics to start and stop kafka instance
 */
class KafkaTestUtil(kafkaPort: Int, zookeeperPort: Int, config: Map[String, String]) {

  private val baseProps = Map(
    "zookeeper.connect" -> s"localhost:$zookeeperPort",
    "enable.zookeeper" -> "true",
    "brokerid" -> "1",
    "listeners" -> s"PLAINTEXT://:$kafkaPort",
    "message.max.bytes" -> "26214400",
    "replica.fetch.max.bytes" -> s"26214400",
    "offsets.topic.replication.factor" -> "1"
  ) ++ config

  private val zkTestServer = new TestingServer(zookeeperPort)
  private val cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString, new RetryOneTime(2000))
  private val kafka = KafkaServerStartable.fromProps(new Properties tap (_ putAll baseProps.asJava))

  /**
   * start associated kafka instance
   */
  def start(): Unit = {
    cli.start()
    kafka.startup()
  }

  /**
   * execute block by starting and then stops kafka instance once done
   */
  def withKafkaRunning[A](f: => A): A = {
    start()
    try f finally stop()
  }

  /**
   * silently stop associated kafka instance
   */
  def stop(): Unit =
    try {
      cli.close()
      kafka.shutdown()
      kafka.awaitShutdown()
      zkTestServer.stop()
    } finally {}
}

object KafkaTestUtil {
  def apply(kafkaPort: Int = 9999, zookeeperPort: Int = 2181, config: Map[String, String] = Map.empty): KafkaTestUtil =
    new KafkaTestUtil(kafkaPort, zookeeperPort, config)
}