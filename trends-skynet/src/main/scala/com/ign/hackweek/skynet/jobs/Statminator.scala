package com.ign.hackweek.skynet.jobs

import com.ign.hackweek.skynet.service.SearchJob
import net.liftweb.common.Loggable
import com.ign.hackweek.skynet.record.TrendRecord
import java.util.Calendar
import collection.mutable.{HashMap, ListBuffer}

class Statminator(name: String, seconds: Int, delay:Int, window: Int, stream: StatStream) extends SearchJob(name, seconds)
  with Loggable {

  var currentStatus = "Idle"
  var lastSnapshots = List[Long]()

  override def status = {
    var parentStatus = super.status
    parentStatus += "status" -> currentStatus
    parentStatus += "lastSnapshots" -> lastSnapshots
    parentStatus
  }

  def dropOldTrends(newSnapshot: List[Long], oldSnapshot: List[Long]) = {
    for (timeFrame <- oldSnapshot) {
      if (!newSnapshot.exists(x => (timeFrame == x))) {
        stream.dropAll(timeFrame)
      }
    }
  }

  def addNewTrends(trendWindow: List[TrendRecord]) = {
    for (trendRecord <- trendWindow) {
      val timeFrame = trendRecord.created.value.getTimeInMillis
      if (!lastSnapshots.exists(x => (x == timeFrame))) {
        val tags = new HashMap[String, Int]()
        for(trend <- trendRecord.trends.value) {
          for(tag <- trend.tags.value) {
            if (tags.contains(tag.name.value))
              tags(tag.name.value) = tags(tag.name.value) + tag.count.value
            else
              tags += tag.name.value -> tag.count.value
          }
        }
        tags.foreach(x => stream.add(x._1,timeFrame,x._2))
      }
    }
  }

  def execute() = {
    val fromTime = Calendar.getInstance
    fromTime.add(Calendar.MINUTE,delay)
    val lastTime = fromTime
    lastTime.add(Calendar.HOUR,window)

    currentStatus = "Consuming trends..."
    val trendWindow = TrendRecord.getSnapshotWindow(lastTime.getTime)
    val newSnapshot = trendWindow.map(t => t.created.value.getTimeInMillis)
    logger.debug("Snapshot size %d".format(newSnapshot.size))
    currentStatus = "Dropping old trends..."
    this.dropOldTrends(newSnapshot,lastSnapshots)
    currentStatus = "Adding new trends..."
    this.addNewTrends(trendWindow)
    lastSnapshots = newSnapshot
    currentStatus = "Idle"
  }
}
