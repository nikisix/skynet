package com.ign.hackweek.skynet.jobs

import twitter4j.Tweet
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable
import collection.mutable.{ListBuffer, HashMap}

case class TweetMessage(name: String, timeFrame: (Int, Int), tweets: List[Tweet])

class TweetMessageQueue extends Loggable {
  private val lock = new ReentrantLock
  private val internalList = new ListBuffer[TweetMessage]()

  def toMap = internalList.map(x => x.name+"_"+x.timeFrame._1.toString -> x.tweets.size).toMap

  def add(name: String, timeFrame: (Int, Int), tweets: List[Tweet]) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      internalList += TweetMessage(name, timeFrame, tweets)
    } finally {
      this.lock.unlock()
    }
  }

  def consumeByTimeFrame(timeFrame: Int): List[TweetMessage] = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      val consume = internalList.filter(m => (timeFrame >= m.timeFrame._1 && timeFrame < m.timeFrame._2))
      val notConsume = internalList.filter(m => !(timeFrame >= m.timeFrame._1 && timeFrame < m.timeFrame._2))
      internalList.clear()
      internalList.appendAll(notConsume)
      return consume.sortBy(_.tweets.size)
    } finally {
      this.lock.unlock()
    }

  }
}