package com.ign.hackweek.skynet.service

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.common.Loggable
import twitter4j.QueryResult

import com.ign.hackweek.skynet.scheduler._
import com.ign.hackweek.skynet.utils.TwitterSearch
import com.ign.hackweek.skynet.model.SearchFeed

class SearchJob(name: String, ms: Long) extends Job {
  val counter = new AtomicInteger(0)

  def status = Map("name" -> this.name, "tick" -> this.ms, "counter" -> counter.get)

  def execute() = {
    counter.incrementAndGet
  }
}

object SearchService extends Loggable with Scheduler {
  private val lock = new ReentrantLock
  private var jobs = List[Job]()

  def startJobs = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stopJobs
      this.jobs = List(new SearchJob("1",1000), new SearchJob("2",2000))
      this._startJobs
    } finally {
      this.lock.unlock()
    }
    this.status
  }

  def stopJobs = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stopJobs
      this.jobs = List[Job]()
    } finally {
      this.lock.unlock()
    }
    this.status
  }

  def registerFeed = {
    if (this.started) {
      //this.feeds = SearchFeed()
    }
    this.status
  }

  def status = {
    val statusText = if (this.) "Started" else "Stopped"
    Map("status" -> statusText, "jobs" -> this.jobs.map(_.status).toList)
  }

  private def _startJobs = {
    Schedule.restart
    Schedule.schedule(this.scheduler, 'process, TimeSpan(1))
  }

  private def _stop = {
    this.started = false
    Schedule.shutdown
  }
}
