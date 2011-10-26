package com.ign.hackweek.skynet.service

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import net.liftweb.common.Loggable
import net.liftweb.actor.LiftActor
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers.TimeSpan
import twitter4j.QueryResult

import com.ign.hackweek.skynet.utils.TwitterSearch
import com.ign.hackweek.skynet.model.SearchFeed

class Scheduler extends LiftActor with TwitterSearch {
  val counter = new AtomicInteger(0)

  def status = Map("count" -> this.counter.get)

  protected def messageHandler = {
    case 'process => {
      counter.incrementAndGet
      this.search("source:twitter4j #skyrim")
      //Do process of each message here
    }
  }
}

object SearchService extends Loggable {
  private val scheduler = new Scheduler
  private val lock = new ReentrantLock
  private var started = false
  private var feeds = List[SearchFeed]()

  def start = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stop
      this.feeds = SearchFeed.findAll
      this._start
    } finally {
      this.lock.unlock()
    }
    this.status
  }

  def stop = {
    this.lock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stop
      this.feeds = List[SearchFeed]()
    } finally {
      this.lock.unlock()
    }
    this.status
  }

  def newFeed = {
    if (this.started) {
      //this.feeds = SearchFeed()
    }
    this.status
  }

  def status = {
    val statusText = if (this.started) "Started" else "Stopped"
    Map("status" -> statusText, "feeds" -> this.feeds.map(_.name).toList, "processed" -> this.scheduler.status)
  }

  private def _start = {
    this.started = true
    Schedule.restart
    Schedule.schedule(this.scheduler, 'process, TimeSpan(1))
  }

  private def _stop = {
    this.started = false
    Schedule.shutdown
  }
}
