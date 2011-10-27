package com.ign.hackweek.skynet.jobs

import com.ign.hackweek.skynet.service.SearchJob
import com.ign.hackweek.skynet.utils.TwitterSearch
import java.util.Calendar
import net.liftweb.common.Loggable

class Trendminator(name: String, seconds: Int, top: Int, tweetQueue: TweetMessageQueue, trendQueue: TrendMessageQueue) extends SearchJob(name, seconds)
  with TwitterSearch with Loggable {

  var lastTimeFrame = 0
  var currentStatus = "Idle"

  override def status = {
    var parentStatus = super.status
    parentStatus += "lastTimeFrame" -> lastTimeFrame
    parentStatus += "status" -> currentStatus
    parentStatus
  }

  def execute() = {
    val fromTime = Calendar.getInstance
    fromTime.add(Calendar.MINUTE,-12)
    val timeFrame = ((fromTime.getTime.getHours * 60) + fromTime.getTime.getMinutes) * 60
    currentStatus = "Moving top %d on tf %d...".format(top,timeFrame)
    var iterate = 0
    val consumed = tweetQueue.consumeByTimeFrame(timeFrame)
    logger.debug("consumed from tf=%d size=%d".format(timeFrame,consumed.size))
    for (message <- consumed) {
      iterate = iterate + 1
      if (iterate < top) {
        trendQueue.add(timeFrame,message.name,message.tweets)
      }
    }
    lastTimeFrame = timeFrame
    currentStatus = "Idle"
  }
}
