package com.ign.hackweek.skynet.service

import collection.mutable.MutableList
import java.util.concurrent.locks.{ReentrantLock, Lock}
import java.util.concurrent.TimeUnit
import com.ign.hackweek.skynet.streams.{TwitterStreamNode, TwitterStreamConfig}
import net.liftweb.common.Loggable

object StreamsService extends Loggable {

  private var streams = List[TwitterStreamNode]()
  private val streamsLock = new ReentrantLock()

  def start = {
    streamsLock.tryLock(200, TimeUnit.MILLISECONDS)
    try {

      this._stop
      this.streams = TwitterStreamConfig.configuration.map( new TwitterStreamNode(_) ).toList
      logger.debug( "starting streams %d ...".format(this.streams.size) )
      this._start

    } finally {
      streamsLock.unlock()
    }
    this.status
  }

  def stop = {
    streamsLock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      this._stop
      this.streams = List[TwitterStreamNode]()
    } finally {
      streamsLock.unlock()
    }
    this.status
  }

  private def _start = {
    this.streams.foreach { stream =>
      try {
        stream.start
      } catch {
        case ex:Exception =>
          logger.error("_start", ex)
      }
    }
  }

  private def _stop = {
    this.streams.foreach { stream =>
      try {
        stream.stop
      } catch {
        case ex: Exception => logger.error("_stop", ex)
      }
    }
  }

  def status = {
    var result = List[Any]()
    streamsLock.tryLock(200, TimeUnit.MILLISECONDS)
    try {
      result = this.streams.map{ stream =>
        Map("id" -> stream.id, "status" -> stream.status)
      }.toList
    } finally {
      streamsLock.unlock()
    }
    result
  }


}
