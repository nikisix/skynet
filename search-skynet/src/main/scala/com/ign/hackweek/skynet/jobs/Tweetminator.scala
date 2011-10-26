package com.ign.hackweek.skynet.jobs

import com.ign.hackweek.skynet.service.SearchJob
import com.ign.hackweek.skynet.utils.TwitterSearch
import com.ign.hackweek.skynet.model.SearchFeed
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}
import twitter4j.{Tweet, QueryResult}

class Tweetminator(queue: TweetQueue) extends SearchJob("Tweetminator", 60000) with TwitterSearch {

  def drop(from: Date, to: Date, result: QueryResult): List[Tweet] = {
    var tweets = List[Tweet]()
    for(t <- result.getTweets.toArray ) {
      val tweet = t.asInstanceOf[Tweet]
      if (tweet.getCreatedAt.compareTo(from) >= 0 && tweet.getCreatedAt.compareTo(to) < 0)
        tweets :+ tweet
    }
    tweets
  }

  def execute() = {
    val feeds = SearchFeed.findAll
    val format = new SimpleDateFormat("HHmm");
    val fromTime = Calendar.getInstance
    val timeFrame = format.format(fromTime.getTime)
    fromTime.add(Calendar.MINUTE,1)
    val toTime = fromTime
    feeds.foreach(feed =>
      queue.add(feed.name + "_" + timeFrame, this.drop(fromTime.getTime, toTime.getTime, search(feed.query))))
  }
}
