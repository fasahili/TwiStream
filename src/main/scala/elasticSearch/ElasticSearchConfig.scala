package elasticSearch

import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.{Request, Response, RestClient, RestClientBuilder}

object ElasticSearchConfig {
  private val credentials = new UsernamePasswordCredentials("elastic", "xj7rXab-=6uM1XFMU+0_")
  private val credentialsProvider: CredentialsProvider = new BasicCredentialsProvider
  credentialsProvider.setCredentials(AuthScope.ANY, credentials)
  System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\marselhanane\\Downloads\\truststore.jks")
  System.setProperty("javax.net.ssl.trustStorePassword", "xj7rXab-=6uM1XFMU+0_")
  val client: RestClient = RestClient.builder(new HttpHost("localhost", 9200, "https"))
    .setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) =>
      httpClientBuilder
        .setDefaultCredentialsProvider(credentialsProvider)
    ).build()

  def CreateIndexTweets(indexName: String): Unit = {
    try {
      val checkIndex = new Request("HEAD", s"/$indexName")
      val response = client.performRequest(checkIndex)
      if (response.getStatusLine.getStatusCode == 404) {
        val createIndex = new Request("PUT", s"/$indexName")
        createIndex.setJsonEntity(
          """
         {
            |  "settings": {
            |    "number_of_shards": 1,
            |    "number_of_replicas": 1
            |  },
            |  "mappings": {
            |    "dynamic": false,
            |    "properties": {
            |      "timestamp": {
            |        "type": "date",
            |        "format":"strict_date_optional_time||epoch_millis"
            |      },
            |      "text": {
            |        "type": "text",
            |        "fields": {
            |          "keyword": {
            |            "type": "keyword"
            |          }
            |        }
            |      },
            |      "location": {
            |        "type": "text",
            |        "fields": {
            |          "keyword": {
            |            "type": "keyword"
            |          }
            |        }
            |      },
            |      "hashtags": { "type": "keyword" },
            |      "geo": {
            |        "type": "geo_point"
            |      },
            |      "sentiment": {
            |          "type": "keyword"
            |      }
            |    }
            |  }
         }
        """.stripMargin
        )
        val response: Response = client.performRequest(createIndex)
        println("Index created successfully "+ response.getStatusLine)
      }else{
        println("Index already exists "+ response.getStatusLine)
      }
    }catch {
      case e: Exception => println("Error creating index: " + e.getMessage)
    }
  }
  def closeClient(): Unit = client.close()
}
