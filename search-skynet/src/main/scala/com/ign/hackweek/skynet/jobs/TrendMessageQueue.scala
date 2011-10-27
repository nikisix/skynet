package com.ign.hackweek.skynet.jobs

import twitter4j.Tweet
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable
import collection.mutable.{HashMap, ListBuffer}

case class TrendMessage(name: String, tweets: List[Tweet], tags: Map[String, Int])

class TrendMessageQueue extends Loggable {
  private val lock = new ReentrantLock
  private val mapper = new HashMap[Int, ListBuffer[TrendMessage]]()

  def toMap = mapper.map(x => x._1.toString -> x._2.map(y => y.name -> Map("tweets" -> y.tweets.size, "tags" -> y.tags)).toMap).toMap

  def add(timeFrame: Int, name: String, tweets: List[Tweet], tags: Map[String, Int]) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      if (!mapper.contains(timeFrame)) {
        val list = new ListBuffer[TrendMessage]()
        val message = TrendMessage(name, tweets, tags)
        list += message
        mapper += timeFrame -> list
      } else {
        val list = mapper(timeFrame)
        val message = TrendMessage(name, tweets, tags)
        list += message
        mapper(timeFrame) = list
      }
    } finally {
      this.lock.unlock()
    }
  }

/*  def consumeAfterTimeFrame(timeFrame: Int): List[TrendMessage] = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      val consume = internalList.filter(m => (timeFrame < m.timeFrame))
      val notConsume = internalList.filter(m => !(timeFrame < m.timeFrame))
      internalList.clear()
      internalList.appendAll(notConsume)
      return consume.sortBy(_.tweets.size)
    } finally {
      this.lock.unlock()
    }
  }*/
}