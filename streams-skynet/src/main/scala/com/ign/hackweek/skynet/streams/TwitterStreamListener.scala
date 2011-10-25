package com.ign.hackweek.skynet.streams

import twitter4j.{Status,StatusListener,StatusDeletionNotice}
import java.util.concurrent.atomic.AtomicInteger

//new TwitterStreamListener(streamConfig.label, streamConfig.queue, streamConfig.postProcs)
class TwitterStreamListener (name: String, queue:MessageQueue,  postProcs:List[PostProcessor])
	extends StatusListener {

  val servedTweets = new AtomicInteger(0)

  def status = Map("servedTweets" -> servedTweets.get())

  override def onStatus(status: Status) = {
      servedTweets.incrementAndGet
      println("Node:" + this.name + "\n" + status.getUser.getName + " : " + status.getText)
  }

  override def onScrubGeo(userId: Long, upToStatusId: Long) = println("onScrub")

  override def onTrackLimitationNotice(number: Int) = println("onTrack")

  override def onDeletionNotice(notice: StatusDeletionNotice) = println("onDeletion")

  override def onException(ex: Exception) = println("onException")
}
