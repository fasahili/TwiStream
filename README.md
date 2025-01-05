# TwiStream

This project implements a complete data pipeline for processing and visualizing tweets in real-time. It ingests streaming tweets, processes the data using Apache Kafka and Elasticsearch, and visualizes the results with Kibana.

## Project Components

1. **Stream Ingestion**
   - Simulated tweet stream ingested using a JSON file.
   - Apache Kafka is used to store and distribute tweets for scalability.

2. **Processing**
   - Tweets are processed to extract key information:
     - Sentiment analysis using Spark NLP.
     - Hashtags, text, timestamps, and geo-coordinates.
   - Tweets are indexed in Elasticsearch for efficient querying.

3. **Storage**
   - Processed tweets are stored in Elasticsearch.
   - Elasticsearch schema supports efficient querying by:
     - Text (e.g., keywords in tweets).
     - Time (timestamps).
     - Space (geo-coordinates).

4. **Visualization**
   - **Kibana** is used for:
     - Mapping tweets on a map based on geo-coordinates.
     - Displaying trends in tweet frequency over time (hourly and daily).
     - Visualizing sentiment analysis with a gauge.

## Pipeline Overview

### Pipeline Details

1. **Data Stream**:
   - Tweets are simulated from `boulder_flood_geolocated_tweets.json` and sent to the Kafka topic `myTweet`.

2. **Kafka Producers and Consumers**:
   - **Producer**:
     - Reads tweets from the JSON file and sends them to the Kafka topic.
     - Configured with batching for performance.
   - **Consumer**:
     - Reads tweets from Kafka, performs sentiment analysis, and transforms the data.
     - Adds the transformed tweets to Elasticsearch.

3. **Elasticsearch**:
   - Stores processed tweets with a schema for text, time, space, hashtags, and sentiment.

4. **Visualization with Kibana**:
   - Maps tweets based on geo-coordinates.
   - Shows temporal tweet trends.
   - Displays average sentiment scores.

## Installation and Setup

### Prerequisites
- Java 
- Apache Kafka
- Elasticsearch and Kibana
- Scala
- sbt (Scala Build Tool)
- Spark NLP library
- Internet connection for downloading pretrained NLP pipelines


## Installation and Setup

### Prerequisites
- **Kafka**: [Kafka Quickstart](https://kafka.apache.org/quickstart)
- **Elasticsearch**: [Elasticsearch Installation](https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html)
- **Kibana**: [Kibana Installation](https://www.elastic.co/guide/en/kibana/current/install.html)

### Configure Elasticsearch
Update the `ElasticSearchConfig.scala` file with the following:
- Elasticsearch credentials.
- Truststore path and password (if required).

### Start Kafka
To start Kafka, use the following commands:
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

### Run Producers and Consumers
Producer:
Run the Kafka producer to ingest tweets into the Kafka topic:
```bash
sbt run kafka.Producer
```

Consumer:
Run the Kafka consumer to process tweets and send them to Elasticsearch:
```bash
sbt run kafka.Consumer
```

## Visualize in Kibana
### Configure Kibana to connect to your Elasticsearch instance.
Set up visualizations to:
- Display tweet trends over time.
- Map tweets based on geo-coordinates.
- Visualize sentiment analysis results.
  
## Running the Project
Steps to Execute
- Start Kafka and Elasticsearch.
- Run the Kafka producer to ingest tweets.
- Run the Kafka consumer to process and store tweets.
- Use Kibana to visualize the results.
  
## Links from which we took the information
- [How to get started with ElasticSearch using Scala client](https://stackoverflow.com/questions/27203498/how-to-get-started-with-elastic-search-using-scala-client)
- [Elasticsearch Date Format Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/date.html?utm_source=chatgpt.com)
![image](https://github.com/user-attachments/assets/b5b89a67-748d-4faa-8d46-f4a6600eb011)

