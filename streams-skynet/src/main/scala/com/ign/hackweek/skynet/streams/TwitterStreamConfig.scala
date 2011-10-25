package com.ign.hackweek.skynet.streams

import twitter4j.FilterQuery
import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory


//Todo: Use a DSL to define the configuration, checkout https://github.com/fogus/baysick/blob/master/src/fogus/baysick/Baysick.scala
case class TwitterStreamConfig(label: String, credentials: (String, String), filterQuery: FilterQuery, postProcs: List[PostProcessor], queue: MessageQueue)

trait PostProcessor {}

case class WhiteListProcessor(lists: List[String]) extends PostProcessor

case class MessageQueue(name: String)


object MessageQueue {

}

class Producer(factory: ConnectionFactory, queueName: String) {

  @volatile
  private var isUp = false

  private var connection = factory.createConnection()
  connection.start()

  private var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

  private var destination = session.createQueue(queueName)

  private var producer = session.createProducer(destination)

  isUp = true

  def send( message:String ) = {
    var _message = session.createTextMessage(message)
    producer.send(_message)
  }

  def close() = {
    if (connection != null) {
      connection.close()
      isUp = false
    }
  }

  def closed_? = {
    if ( connection != null ) isUp else false
  }
}

object TwitterStreamConfig {

  val configuration: List[TwitterStreamConfig] = new TwitterStreamConfig("a label", ("skynetign", "abcd1234"),
    new FilterQuery(0, null,
      Array("play", "game"),
      Array(Array(-122.75, 36.8), Array(-121.75, 37.8))),

    WhiteListProcessor("play" :: "game" :: Nil) :: Nil,

    MessageQueue("raw.queue")

  ) :: Nil

}