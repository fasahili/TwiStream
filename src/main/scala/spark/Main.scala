package spark

import elasticSearch.ElasticSearchConfig.{CreateIndexTweets, client}
import org.apache.http.util.EntityUtils
import org.apache.spark.sql.SparkSession
import org.elasticsearch.client.Request

object Main {
  def main(args: Array[String]): Unit = {
    // عشان اعمل تست لل elastic
    val response = client.performRequest(
      new Request("GET", "/")
    )
    println(EntityUtils.toString(response.getEntity))
    // اعمل تست اذا بنشئ اندكس او لا
    CreateIndexTweets("try")
    /*val spark = SparkSession.builder
      .appName("TwiStream")
      .master("local[*]")
      .getOrCreate()

    val kafkaStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "tweets")
      .option("startingOffsets", "earliest")
      .load()

    val messages = kafkaStream.selectExpr("CAST(value AS STRING)")

    val query = messages.writeStream
      .outputMode("append")
      .format("console")
      .start()

    query.awaitTermination()*/
  }
}
