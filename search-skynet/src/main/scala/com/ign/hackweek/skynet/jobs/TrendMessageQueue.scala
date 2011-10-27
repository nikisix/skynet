package com.ign.hackweek.skynet.jobs

import twitter4j.Tweet
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable
import collection.mutable.{ListBuffer}

case class TrendMessage(timeFrame: Int, name: String, tweets: List[Tweet])

class TrendMessageQueue extends Loggable {
  private val lock = new ReentrantLock
  private val internalList = new ListBuffer[TrendMessage]()

  def toMap = internalList.map(x => x.timeFrame.toString+"_"+x.name -> x.tweets.size).toMap

  def add(timeFrame: Int, name: String, tweets: List[Tweet]) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      internalList += TrendMessage(timeFrame, name, tweets)
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