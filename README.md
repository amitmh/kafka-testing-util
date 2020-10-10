#### Kafka Test Util

Provides wrapper around [KafkaServerStartable](https://github.com/apache/kafka/blob/trunk/core/src/main/scala/kafka/server/KafkaServerStartable.scala)

##### Usage

The simplest way to execute `block` while making sure a kafka instance is running:

```scala 
 KafkaTestUtil().withKafkaRunning {
    // block
}
```

##### Overriding configuration

Kafka startup properties can be overwritten using constructor methods.
Note that `config` overrides default `kafkaPort` on conflict.

```
    KafkaTestUtil(kafkaPort = 9092, zookeeperPort= 2181, config = Map.empty)
```
