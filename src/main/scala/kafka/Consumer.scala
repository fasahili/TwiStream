package kafka

import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import org.apache.kafka.clients.consumer.KafkaConsumer
import play.api.libs.json.{JsObject, Json}
import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoUnit
import java.util.{Collections, Properties}
import scala.collection.JavaConverters._

object Consumer {
  def main(args: Array[String]): Unit = {
    val topic = "tweets_topic"
    val batchSize = 100
    val delay = 5000
    val maxTweets = 1000

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("group.id", "tweet-consumer-group")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "earliest")

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList(topic))

    println(s"Consuming messages from topic: $topic")

    var tweetCount = 0

    try {
      while (tweetCount < maxTweets) {
        val records = consumer.poll(java.time.Duration.ofMillis(1000))
        val tweetsToProcess = records.asScala.take(batchSize)

        for (record <- tweetsToProcess) {
          val tweetJson = Json.parse(record.value())

          val geoJsonOpt = (tweetJson \ "geo").asOpt[JsObject]
          val geoField = geoJsonOpt match {
            case Some(geo) =>
              val latitude = (geo \ "latitude").asOpt[Double].getOrElse(0.0)
              val longitude = (geo \ "longitude").asOpt[Double].getOrElse(0.0)
              Json.obj("geo" -> Json.arr(longitude, latitude))
            case None => Json.obj()
          }

          val sentiment = analyzeSentiment((tweetJson \ "text").asOpt[String].getOrElse(""))

          val updatedTweetJson = Json.obj(
            "created_at" -> convertToISO8601((tweetJson \ "created_at").asOpt[String].getOrElse("Unknown Date")),
            "text" -> (tweetJson \ "text").asOpt[String].getOrElse("No text available"),
            "hashtags" -> (tweetJson \ "hashtags").asOpt[Seq[String]].getOrElse(Seq.empty),
            "location" -> (tweetJson \ "location").asOpt[String].getOrElse("Unknown location"),
            "sentiment" -> sentiment
          ) ++ geoField

          println(s"Processed tweet: $updatedTweetJson")
        }

        tweetCount += tweetsToProcess.size
        println(s"Processed $tweetCount tweets so far.")

        if (tweetCount < maxTweets) {
          Thread.sleep(delay)
        }
      }

      println(s"Finished processing $tweetCount tweets.")
    } catch {
      case ex: Exception =>
        println(s"Error while consuming messages: ${ex.getMessage}")
    } finally {
      consumer.close()
    }
  }

  private def convertToISO8601(dateString: String): String = {
    try {
      val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
      val date = ZonedDateTime.parse(dateString, formatter)
      date.truncatedTo(ChronoUnit.SECONDS).toString
    } catch {
      case _: DateTimeParseException => "Unknown Date"
    }
  }

  private val sentimentPipeline = PretrainedPipeline("analyze_sentiment", lang = "en")
  private def analyzeSentiment(text: String): String = {
    if (text.isEmpty) "Neutral"
    else sentimentPipeline.annotate(text)("sentiment").headOption.getOrElse("Neutral")
  }
}