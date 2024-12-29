package kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Collections, Properties}
import scala.collection.JavaConverters._
import play.api.libs.json.{Json, JsArray, JsObject}
import elasticSearch.ElasticSearchConfig._
import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoUnit
import org.elasticsearch.client.Request
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline

object Consumer {
  private val sentimentPipeline = PretrainedPipeline("analyze_sentiment", lang = "en")

  def main(args: Array[String]): Unit = {
    val topic = "try"
    val indexName = "test"
    val batchSize = 100
    val delay = 1000
    val maxTweets = 1000
    createIndex(indexName)

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
    var processedBatch = 0

    try {
      while (tweetCount < maxTweets) {
        val records = consumer.poll(java.time.Duration.ofMillis(1000))
        val tweetsToProcess = records.asScala.take(batchSize)

        for (record <- tweetsToProcess) {
          val tweetJson = Json.parse(record.value())
          val sentiment = analyzeSentiment((tweetJson \ "text").asOpt[String].getOrElse(""))
          val updatedTweetJson = {
            val geoField = (tweetJson \ "geo").asOpt[JsArray].map(geoArray =>
              Json.obj("geo" -> geoArray)
            ).getOrElse(Json.obj())

            Json.obj(
              "created_at" -> Json.toJson(convertToISO8601((tweetJson \ "created_at").as[String])),
              "text" -> Json.toJson((tweetJson \ "text").asOpt[String].getOrElse("No text available")),
              "location" -> Json.toJson((tweetJson \ "location").asOpt[String].getOrElse("Unknown location")),
              "hashtags" -> Json.toJson((tweetJson \ "hashtags").asOpt[Seq[String]].getOrElse(Seq.empty)),
              "sentiment" -> Json.toJson(sentiment)
            ) ++ geoField
          }
          addDocumentToElastic(indexName, updatedTweetJson)
          println(updatedTweetJson)
        }

        tweetCount += tweetsToProcess.size
        processedBatch += 1

        println(s"Processed $tweetCount tweets so far.")
        if (processedBatch % (batchSize / 100) == 0) {
          println(s"Batch processed. Pausing for $delay milliseconds...")
          Thread.sleep(delay)
        }
      }

      println(s"Finished processing $tweetCount tweets.")
    } catch {
      case ex: Exception =>
        println(s"Error while consuming messages: ${ex.getMessage}")
        ex.printStackTrace()
    } finally {
      consumer.close()
      closeClient()
    }
  }

  private def analyzeSentiment(text: String): String = {
    if (text.isEmpty) "Neutral"
    else sentimentPipeline.annotate(text)("sentiment").headOption.getOrElse("Neutral")
  }

  private def convertToISO8601(dateString: String): String = {
    try {
      val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")
      val date = ZonedDateTime.parse(dateString, formatter)
      date.truncatedTo(ChronoUnit.SECONDS).toString
    } catch {
      case e: DateTimeParseException =>
        println(s"Failed to parse date: $dateString")
        "Unknown Date"
    }
  }

  private def addDocumentToElastic(indexName: String, document: JsObject): Unit = {
    try {
      val request = new Request("POST", s"/$indexName/_doc")
      request.setJsonEntity(document.toString())
      val response = client.performRequest(request)
      println(s"Document added to Elasticsearch with response: ${response.getStatusLine}")
    } catch {
      case ex: Exception =>
        println(s"Error while adding document to Elasticsearch: ${ex.getMessage}")
        ex.printStackTrace()
    }
  }
}
