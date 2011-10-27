package com.ign.hackweek.skynet.jobs

import com.ign.hackweek.skynet.service.SearchJob
import com.ign.hackweek.skynet.utils.TwitterSearch
import com.ign.hackweek.skynet.model.SearchFeed
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}
import twitter4j.{Tweet, QueryResult}
import net.liftweb.common.Loggable
import collection.mutable.ListBuffer

class Tweetminator(name: String, seconds: Int, queue: TweetMessageQueue) extends SearchJob(name, seconds)
  with TwitterSearch with Loggable {

  var currentStatus = "Idle"
  var lastFeedCount = 0

  override def status = {
    var parentStatus = super.status
    parentStatus += "lastFeedCount" -> lastFeedCount
    parentStatus += "status" -> currentStatus
    parentStatus
  }

  def drop(timeFrame: Int, result: QueryResult): List[Tweet] = {
    val tweets = new ListBuffer[Tweet]()
    val preTweets = result.getTweets.toArray
    for(t <- preTweets) {
      val tweet = t.asInstanceOf[Tweet]
      val created = ((tweet.getCreatedAt.getHours * 60) + tweet.getCreatedAt.getMinutes) * 60
      logger.debug("Tweet: " + created.toString + " / " + timeFrame.toString + " v " + (timeFrame+seconds).toString)
      if (created >= timeFrame && created < timeFrame+seconds) {
        tweets += tweet
      }
    }
    tweets.toList
  }

  def execute() = {
    currentStatus = "Finding feeds..."
    val feeds = SearchFeed.findAll
    lastFeedCount = feeds.size
    val fromTime = Calendar.getInstance
    fromTime.add(Calendar.MINUTE,-10)
    val timeFrame = ((fromTime.getTime.getHours * 60) + fromTime.getTime.getMinutes) * 60
    for (feed <- feeds) {
      currentStatus = "Searching feed %s...".format(feed.name)
      val preTweets = search(feed.query)
      if (preTweets != null) {
        val listValue = this.drop(timeFrame, preTweets)
        queue.add(feed.name, (timeFrame, seconds), listValue)
      }
    }
    currentStatus = "Idle"
  }
}
