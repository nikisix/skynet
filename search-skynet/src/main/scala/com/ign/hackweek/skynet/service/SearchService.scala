package com.ign.hackweek.skynet.service

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable

import com.ign.hackweek.skynet.scheduler._
import com.ign.hackweek.skynet.model.SearchFeed
import net.liftweb.json.{DefaultFormats, JsonAST}
import com.mongodb.WriteConcern
import com.ign.hackweek.skynet.jobs.{Trendminator, TrendMessageQueue, Tweetminator, TweetMessageQueue}

abstract class SearchJob(name: String, seconds: Int) extends Job {
  def schedule = JobSchedule.repeat(this.seconds * 1000)
  def status = Map("name" -> this.name, "tick" -> this.seconds)
}

object SearchService extends Loggable with Scheduler {

  implicit val formats = DefaultFormats

  this.start()

  private val lock = new ReentrantLock
  private var jobs = List[SearchJob]()
  private val tweetQueue: TweetMessageQueue = new TweetMessageQueue

  def startJobs = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stopJobs
      this.jobs = List(
                    new Tweetminator("Tweetminator",600,-10,tweetQueue),
                    new Trendminator("Trendminator",200,-15,tweetQueue)
                  )
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

  def registerFeed(json: JsonAST.JValue) = {
    val feed = json.extract[SearchFeed]
    val record = feed.toRecord
    record.save(WriteConcern.SAFE)
    Map("id" -> record.id)
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
    Map(
      "status" -> statusText,
      "jobs" -> jobStatuses,
      "tweetQueue" -> this.tweetQueue.toMap
    )
  }

  private def _startJobs = {
    this.jobs.foreach( job => this.add(job,job.schedule) )
  }

  private def _stopJobs = {
    this.cancelAll()
  }
}
