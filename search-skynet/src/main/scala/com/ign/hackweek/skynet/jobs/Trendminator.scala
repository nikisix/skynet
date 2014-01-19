package com.ign.hackweek.skynet.jobs

import com.ign.hackweek.skynet.service.SearchJob
import com.ign.hackweek.skynet.utils.TwitterSearch
import java.util.Calendar
import net.liftweb.common.Loggable
import twitter4j.{HashtagEntity, Status}
import collection.mutable.{HashMap,ListBuffer}

import com.mongodb.WriteConcern
import com.ign.hackweek.skynet.record.{TweetTag, TweetObject, Trend, TrendRecord}

class Trendminator(name: String, seconds: Int, delay: Int, tweetQueue: TweetMessageQueue) extends SearchJob(name, seconds)
  with TwitterSearch with Loggable {

  var lastTimeFrame = 0
  var currentStatus = "Idle"

  override def status = {
    var parentStatus = super.status
    parentStatus += "lastTimeFrame" -> lastTimeFrame
    parentStatus += "status" -> currentStatus
    parentStatus
  }

  def trendByTags(tweets: List[Status]): List[TweetTag] = {
    var tags = new HashMap[String, Int]()
    tweets.foreach(_.getHashtagEntities.foreach(tag => {
      val tagText = tag.getText.toLowerCase
      if (!tags.contains(tagText))
        tags += tagText -> 1
      else
        tags(tagText) = tags(tagText) + 1
    }))
    var listTags = new ListBuffer[TweetTag]()
    for (tag <- tags) {
      val newTag = TweetTag.createRecord.name(tag._1).count(tag._2)
      listTags += newTag
    }
    listTags.toList
  }

  def execute() = {
    val fromTime = Calendar.getInstance
    fromTime.add(Calendar.MINUTE,delay)
    val timeFrame = ((fromTime.getTime.getHours * 60) + fromTime.getTime.getMinutes) * 60
    currentStatus = "Moving top on tf %d...".format(timeFrame)
    var iterate = 0
    val consumed = tweetQueue.consumeByTimeFrame(timeFrame)
    logger.debug("consumed from tf=%d size=%d".format(timeFrame,consumed.size))

    var trends = new ListBuffer[Trend]()
    val newRecord = TrendRecord.createRecord
    newRecord.timeFrame(timeFrame)
    for (message <- consumed) {
      if (message.tweets.size > 0) {
        val trend = Trend.createRecord
        val tags = this.trendByTags(message.tweets)
        val tweets = new ListBuffer[TweetObject]()
        for (tweet <- message.tweets) {
          tweets += TweetObject.createFromTweet(tweet)
        }
        trends += trend.name(message.name).tags(tags).tweets(tweets.toList)
      }
    }
    if (trends.size > 0) {
      newRecord.trends(trends.toList)
      newRecord.save(WriteConcern.SAFE)
    }
    lastTimeFrame = timeFrame
    currentStatus = "Idle"
  }
}
