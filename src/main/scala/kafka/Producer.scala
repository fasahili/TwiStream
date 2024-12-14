package kafka

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.Properties

object Producer {
  def main(args: Array[String]): Unit = {
    val topic = "tweets"

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val message = "{\"text\": \"Hello Kafka!\", \"location\": \"World\"}"
    producer.send(new ProducerRecord[String, String](topic, null, message))

    println("Message sent to Kafka!")
    producer.close()
  }
}
