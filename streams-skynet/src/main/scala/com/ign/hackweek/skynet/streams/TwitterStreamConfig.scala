package com.ign.hackweek.skynet.streams

import twitter4j.FilterQuery
import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory


//Todo: Use a DSL to define the configuration, checkout https://github.com/fogus/baysick/blob/master/src/fogus/baysick/Baysick.scala
case class TwitterStreamConfig(label:String, credentials:(String,String), filterQuery:FilterQuery, postProcs:List[PostProcessor], queue:MessageQueue )

trait PostProcessor{}

case class WhiteListProcessor( lists:List[String] ) extends PostProcessor

case class MessageQueue(name:String)


object MessageQueue {

}

class Producer(factory:ConnectionFactory, queueName:String) {
    // private ConnectionFactory factory;
    // private Connection connection;
    // private Session session;
    // private MessageProducer producer;

    private var connection = factory.createConnection()
    connection.start()
    private var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    private var destination = session.createQueue(queueName)
    private var producer = session.createProducer(destination)

    def run() = {
        for (i <- 0 until 100) {
            println("Sending message")
            var message = session.createTextMessage("Number " + i + ": Hello world!")
            producer.send(message)
        }
    }

    def close() = {
        if (connection != null) { connection.close() }
    }
}


object ProducerApp extends Application {
  val brokerUrl = "tcp://localhost:61616"

  val factory:ConnectionFactory = new ActiveMQConnectionFactory(brokerUrl)
  val producer:Producer = new Producer(factory, "test")
  producer.run()
  producer.close()
}

object TwitterStreamConfig {

  val configuration:List[TwitterStreamConfig] = new TwitterStreamConfig("a label", ( "skynetign", "abcd1234"),
    new FilterQuery(0, null,
					Array("play","game"),
					Array(Array(-122.75,36.8), Array(-121.75,37.8))),

    WhiteListProcessor( "play" :: "game" :: Nil ) :: Nil,

    MessageQueue("raw.queue")

  ) :: Nil

}