package kafka

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json._

import java.util.Properties
import scala.io.Source

object Producer {
  def main(args: Array[String]): Unit = {
    val topic = "try"

    // إعداد خصائص الاتصال بكافكا
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    // إنشاء منتج Kafka
    val producer = new KafkaProducer[String, String](props)
    val filePath = "src/main/scala/data/boulder_flood_geolocated_tweets.json"

    // قراءة الملف وإرسال كل تغريدة بعد معالجتها
    val source = Source.fromFile(filePath)
    try {
      source.getLines()
        .map { line =>
          // تحويل كل سطر إلى JSON ومعالجة البيانات
          val record = Json.parse(line)
          val createdAt = (record \ "created_at").asOpt[String].getOrElse("N/A")
          val text = (record \ "text").asOpt[String].getOrElse("N/A")
          val hashtags = (record \ "entities" \ "hashtags").asOpt[Seq[JsValue]].getOrElse(Seq())
            .map(tag => (tag \ "text").asOpt[String].getOrElse(""))

          val location = (record \ "user" \ "location").asOpt[String].getOrElse("Unknown")
          val geoCoordinates = (record \ "geo" \ "coordinates").asOpt[Seq[Double]]

          // تكوين البيانات التي سيتم إرسالها بناءً على وجود geoCoordinates
          val filteredData = {
            val baseData = Json.obj(
              "created_at" -> createdAt,
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

          filteredData
        }
        .foreach { filteredData =>
          // إرسال التغريدة المفلترة إلى Kafka
          producer.send(new ProducerRecord[String, String](topic, null, filteredData.toString()))
          println(s"Sent: $filteredData")
        }
    } catch {
      case ex: Exception =>
        println(s"Error opening file: $filePath. Error: ${ex.getMessage}")
    } finally {
      source.close()
      producer.close()
    }
  }
}