package com.ign.hackweek.skynet.jobs

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable
import collection.mutable.HashMap

class StatStream extends Loggable {
  private val lock = new ReentrantLock
  private val mapper = new HashMap[String, HashMap[Long, Double]]()

  def toMap = mapper.map(x => x._1 -> x._2.size).toMap

  def dropAll(timeFrame: Long) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      mapper.foreach(x => x._2.remove(timeFrame))
    } finally {
      this.lock.unlock()
    }
  }

  def add(tagNode: String, timeFrame: Long, count: Double) = {
    this.lock.tryLock(100, TimeUnit.MILLISECONDS)
    try {
      if (mapper.contains(tagNode)) {
        mapper(tagNode) += timeFrame -> count
      } else {
        val newMap = new HashMap[Long, Double]()
        newMap += timeFrame -> count
        mapper += tagNode -> newMap
      }
    } finally {
      this.lock.unlock()
    }
  }
}