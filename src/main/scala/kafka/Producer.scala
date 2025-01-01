package kafka
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json._
import java.util.Properties
import scala.io.Source
import scala.concurrent.duration._

object Producer {
  def main(args: Array[String]): Unit = {
    val topic = "myTweet"
    val batchSize = 100
    val delay = 3.seconds

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val filePath = "src/main/scala/data/boulder_flood_geolocated_tweets.json"
    try {
      val source = Source.fromFile(filePath)
      try {
        var tweetBatch = List[String]()
        for (line <- source.getLines()) {
          try {
            val tweetJson = Json.parse(line)
            val createdAt = (tweetJson \ "created_at").asOpt[String].getOrElse("N/A")
            val text = (tweetJson \ "text").asOpt[String].getOrElse("N/A")
            val hashtags = (tweetJson \ "entities" \ "hashtags").asOpt[Seq[JsValue]].getOrElse(Seq())
              .map(tag => (tag \ "text").asOpt[String].getOrElse(""))
            val location = (tweetJson \ "user" \ "location").asOpt[String].getOrElse("Unknown")
            val geoCoordinates = (tweetJson \ "geo" \ "coordinates").asOpt[Seq[Double]]
            val filteredData = {
              val baseData = Json.obj(
                "timestamp" -> createdAt,
                "text" -> text,
                "hashtags" -> hashtags,
                "location" -> location
              )
              geoCoordinates match {
                case Some(Seq(lat, lon)) =>
                  baseData ++ Json.obj("geo" -> Json.arr(lon, lat))
                case None => baseData
              }
            }
            tweetBatch = tweetBatch :+ filteredData.toString()
            if (tweetBatch.size >= batchSize) {
              println(s"Batch of $batchSize tweets to be sent:")
              tweetBatch.foreach(println)
              sendBatch(producer, tweetBatch, topic)
              tweetBatch = List()
              println(s"Sent batch of $batchSize tweets")
              Thread.sleep(delay.toMillis)
            }
          } catch {
            case ex: Exception =>
              println(s"Failed to process line: $line. Error: ${ex.getMessage}")
          }
        }
        if (tweetBatch.nonEmpty) {
          println(s"Remaining batch to be sent:")
          tweetBatch.foreach(println)
          sendBatch(producer, tweetBatch, topic)
          println(s"Sent remaining batch of ${tweetBatch.size} tweets")
        }
      } finally {
        source.close()
      }
    } catch {
      case ex: Exception =>
        println(s"Error opening file: $filePath. Error: ${ex.getMessage}")
    }
    producer.close()
  }
  def sendBatch(producer: KafkaProducer[String, String], batch: List[String], topic: String): Unit = {
    batch.foreach { tweet =>
      producer.send(new ProducerRecord[String, String](topic, null, tweet))
    }
  }
}