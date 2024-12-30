package kafka

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json._

import java.util.Properties
import scala.io.Source
import scala.concurrent.duration._

object Producer {
  def main(args: Array[String]): Unit = {
    val topic = "try"
    val properties = new Properties()
    properties.put("bootstrap.servers", "localhost:9092")
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](properties)
    val filePath = "src/main/scala/data/boulder_flood_geolocated_tweets.json"

    val source = Source.fromFile(filePath)
    try {
      val records = source.getLines().map { line =>
        val record = Json.parse(line)
        val createdAt = (record \ "created_at").headOption.flatMap(_.asOpt[String]).getOrElse("N/A")
        val text = (record \ "text").headOption.flatMap(_.asOpt[String]).getOrElse("N/A")
        val hashtags = (record \ "entities" \ "hashtags").headOption.flatMap(_.asOpt[Seq[JsValue]]).getOrElse(Seq())
          .map(tag => (tag \ "text").headOption.flatMap(_.asOpt[String]).getOrElse(""))
        val location = (record \ "user" \ "location").headOption.flatMap(_.asOpt[String]).getOrElse("Unknown")
        val geoCoordinates = (record \ "geo" \ "coordinates").headOption.flatMap(_.asOpt[Seq[Double]])

        val filteredData = {
          val kafkaValues = Json.obj(
            "created_at" -> createdAt,
            "text" -> text,
            "hashtags" -> hashtags,
            "location" -> location
          )
          geoCoordinates match {
            case Some(Seq(lat, lon)) =>
              kafkaValues ++ Json.obj("geo" -> Json.arr(lon, lat))
            case None => kafkaValues
          }
        }

        filteredData.toString()
      }.toSeq

      records.grouped(1000).foreach { batch =>
        batch.foreach { filteredData =>
          producer.send(new ProducerRecord[String, String](topic, null, filteredData))
          println(s"Sent: $filteredData")
        }
        Thread.sleep(1000)
      }

    } catch {
      case ex: Exception =>
        println(s"Error processing file: $filePath. Error: ${ex.getMessage}")
    } finally {
      source.close()
      producer.close()
    }
  }
}
