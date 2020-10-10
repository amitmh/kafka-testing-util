package github.amitmh.kafkatestutil

import java.util.Properties

import kafka.server.KafkaServerStartable
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters.mapAsJavaMapConverter

/**
 * provides mechanics to start and stop kafka instance
 * Note that `config` overrides default `kafkaPort` on conflict.
 */
class KafkaTestUtil(kafkaPort: Int, zookeeperPort: Int, config: Map[String, String]) {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  private val baseProps = Map(
    "enable.zookeeper" -> "true",
    "brokerid" -> "1",
    "listeners" -> s"PLAINTEXT://:$kafkaPort",
    "message.max.bytes" -> s"${1 << 26}",
    "replica.fetch.max.bytes" -> s"${1 << 26}",
    "offsets.topic.replication.factor" -> "1"
  ) ++ config ++ Map("zookeeper.connect" -> s"localhost:$zookeeperPort")

  logger.debug(s"kafka startup config is $baseProps")

  private val zkTestServer = new TestingServer(zookeeperPort)
  private val cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString, new RetryOneTime(2000))
  private val kafka = KafkaServerStartable.fromProps(new Properties tap (_ putAll baseProps.asJava))

  /**
   * start associated kafka instance
   */
  def start(): Unit = {
    logger.trace(s"starting kafka")
    cli.start()
    kafka.startup()
    logger.debug(s"kafka started successfully.")
  }

  /**
   * execute block by starting and then stops kafka instance once done
   */
  def withKafkaRunning[A](block: => A): A = {
    start()
    try block finally stop()
  }

  /**
   * silently stop associated kafka instance
   */
  def stop(): Unit =
    try {
      logger.trace(s"shutting down kafka")
      cli.close()
      kafka.shutdown()
      kafka.awaitShutdown()
      zkTestServer.stop()
      logger.debug(s"kafka shutdown was successful")
    } finally {}
}

object KafkaTestUtil {
  def apply(kafkaPort: Int = 9092, zookeeperPort: Int = 2181, config: Map[String, String] = Map.empty): KafkaTestUtil =
    new KafkaTestUtil(kafkaPort, zookeeperPort, config)
}