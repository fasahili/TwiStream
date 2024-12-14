package spark

import org.apache.spark.sql.SparkSession

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
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

    query.awaitTermination()
  }
}
