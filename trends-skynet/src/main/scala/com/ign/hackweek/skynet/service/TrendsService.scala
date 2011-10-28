package com.ign.hackweek.skynet.service

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import net.liftweb.common.Loggable

import com.ign.hackweek.skynet.scheduler._
import net.liftweb.json.{DefaultFormats, JsonAST}
import com.ign.hackweek.skynet.jobs.{StatStream, Statminator}

abstract class SearchJob(name: String, seconds: Int) extends Job {
  def schedule = JobSchedule.repeat(this.seconds * 1000)
  def status = Map("name" -> this.name, "tick" -> this.seconds)
}

object TrendsService extends Loggable with Scheduler {

  implicit val formats = DefaultFormats

  this.start()

  private val lock = new ReentrantLock
  private var jobs = List[SearchJob]()
  private val stream = new StatStream

  def startJobs = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stopJobs
      this.jobs = List(
                    new Statminator("Statminator",30,-30,-24,stream)
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

  def getTrendsByTag(sinceTimeFrame: Long) = {
    val trends = stream.getTrendsByTag(sinceTimeFrame)
    trends.map(x => Map(
      "timeFrame" -> x.name.value,
      "totalTags" -> x.tags.value.size,
      "tags" -> x.tags.value.map(y => Map("tag" -> y.name.value, "count" -> y.count.value))
    ))
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
      "jobs" -> jobStatuses
    )
  }

  private def _startJobs = {
    this.jobs.foreach( job => this.add(job,job.schedule) )
  }

  private def _stopJobs = {
    this.cancelAll()
  }
}
