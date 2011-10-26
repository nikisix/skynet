package com.ign.hackweek.skynet.service

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.common.Loggable
import twitter4j.QueryResult

import com.ign.hackweek.skynet.scheduler._
import com.ign.hackweek.skynet.utils.TwitterSearch
import com.ign.hackweek.skynet.model.SearchFeed
import com.ign.hackweek.skynet.jobs.{Tweetminator, TweetQueue}

abstract class SearchJob(name: String, ms: Long) extends Job {
  def schedule = JobSchedule.repeat(this.ms)

  def status = Map("name" -> this.name, "tick" -> this.ms)
}

object SearchService extends Loggable with Scheduler {
  this.start()

  private val lock = new ReentrantLock
  private var jobs = List[SearchJob]()
  private val messageQueue: TweetQueue = new TweetQueue

  def startJobs = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stopJobs
      this.jobs = List(new Tweetminator(messageQueue))
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
      this.jobs = List[SearchJob]()
    } finally {
      this.lock.unlock()
    }
    this.status
  }

  def registerFeed = {
    this.status
  }

  def status = {
    var jobStatuses: List[Map[String, Any]] = Nil
    var statusText = "Stopped"
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      if (this.jobs.size > 0)
        statusText = "Started"
      jobStatuses = this.jobs.map(_.status).toList
    } finally {
      this.lock.unlock()
    }
    Map("status" -> statusText, "jobs" -> jobStatuses, "tweets" -> this.messageQueue)
  }

  private def _startJobs = {
    this.jobs.foreach( job => this.add(job,job.schedule) )
  }

  private def _stopJobs = {
    this.cancelAll()
  }
}
