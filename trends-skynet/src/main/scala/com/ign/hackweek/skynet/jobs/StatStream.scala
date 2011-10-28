package com.ign.hackweek.skynet.jobs

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable
import java.util.Calendar
import collection.mutable.{ListBuffer, HashMap}
import com.ign.hackweek.skynet.record.{TweetTag, Trend}

class StatStream extends Loggable {
  private val lock = new ReentrantLock
  private val mapperByTag = new HashMap[String, HashMap[Long, Double]]()
  private val mapperByTimeFrame = new HashMap[Long, HashMap[String, Double]]()

  def getTrendsByTag(sinceTimeFrame: Long): List[Trend] = {
    val result = new ListBuffer[Trend]()
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      logger.debug("Current ms: %d".format(Calendar.getInstance.getTimeInMillis))
      logger.debug("Since ms: %d".format(sinceTimeFrame))
      for(tf <- mapperByTimeFrame) {
        if (sinceTimeFrame < tf._1) {
          val trend = Trend.createRecord
          val tags = tf._2.toList.sortBy(x => 0.0 - x._2).map(y => {
            val tag = TweetTag.createRecord
            tag.name(y._1)
            tag.count(y._2.toInt)
            tag
          })
          trend.name(tf._1.toString)
          trend.tags(tags)
          result += trend
        }
      }
    } finally {
      this.lock.unlock()
    }
    result.toList
  }

  def dropAll(timeFrame: Long) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      mapperByTag.foreach(x => x._2.remove(timeFrame))
      mapperByTimeFrame.remove(timeFrame)
    } finally {
      this.lock.unlock()
    }
  }

  def add(tagNode: String, timeFrame: Long, count: Double) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      if (mapperByTag.contains(tagNode)) {
        mapperByTag(tagNode) += timeFrame -> count
      } else {
        val newMap = new HashMap[Long, Double]()
        newMap += timeFrame -> count
        mapperByTag += tagNode -> newMap
      }
      if (mapperByTimeFrame.contains(timeFrame)) {
        mapperByTimeFrame(timeFrame) += tagNode -> count
      } else {
        val newMap = new HashMap[String, Double]()
        newMap += tagNode -> count
        mapperByTimeFrame += timeFrame -> newMap
      }
    } finally {
      this.lock.unlock()
    }
  }
}