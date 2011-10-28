package com.ign.hackweek.skynet.scheduler

import java.util.{Calendar, Date}
import scala.collection.mutable.{ListBuffer,HashMap, PriorityQueue}
import scala.actors._

trait Scheduler {
  private val queue = new PriorityQueue[Scheduled]
  private val groups = new HashMap[String,GroupActor]
  private val thread = new Thread(runnable, "Scheduler")
  private var stopped = false
  protected val clock: Clock = SystemClock

  val defaultGroupName = "default"

  def isRunning() = !this.stopped

  def start() {
    if (!thread.isAlive) {
      stopped = false
      thread.setDaemon(true)
      thread.start()
    }
  }

  def stop() {
    stopped = true
    cancelAll()
    thread.interrupt()
  }

  def add(job: Job, schedule: JobSchedule): JobHandle = {
    add(job, schedule, defaultGroupName)
  }

  def add(job: Job, schedule: JobSchedule, groupName: String): JobHandle = {
    add(job, schedule, getGroup(groupName))
  }

  private def add(job: Job, schedule: JobSchedule, group: GroupActor): JobHandle = {
    val handle = JobHandleImpl(job, schedule, group)
    val scheduled = Scheduled(handle, schedule.firstRun(clock.now), 0)
    queue.synchronized {
      queue += scheduled
      queue.notify
    }
    handle
  }

  def cancel(handle: JobHandle) {
    queue.synchronized {
      val entries = queue.filter(_.handle != handle)
      queue.clear()
      queue ++= entries
    }
  }

  def cancelAll() {
    queue.synchronized {
      queue.clear()
    }
  }

  def peek: Option[(JobHandle, Long)] = {
    queue.synchronized {
      if (queue.isEmpty)
        None
      else {
        val max = queue.max
        Some((max.handle, max.nextRun))
      }
    }
  }

  def isEmpty = queue.isEmpty

  private def getGroup(groupName: String): GroupActor = {
    groups.synchronized {
      groups.get(groupName).getOrElse {
        val g = new GroupActor(groupName)
        groups(groupName) = g
        g.start()
        g
      }
    }
  }

  protected def tick: Option[Long] = {
    queue.synchronized {
      if (queue.isEmpty) {
        Some(0)
      }
      else {
        val currentTime = clock.now
        val nextRunTime = queue.max.nextRun
        if (nextRunTime > currentTime) {
          Some(nextRunTime - currentTime)
        }
        else {
          queue.dequeue match {
            case Scheduled(handle, scheduledTime, _) =>
              handle.group ! RunJob(handle.job)
              handle.schedule.nextRun(scheduledTime).foreach { nextTime =>
                queue += Scheduled(handle, nextTime, scheduledTime)
              }
          }
          None
        }
      }
    }
  }

  private def runnable = new Runnable {
    def run() {
      while (!stopped) {
        tick match {
          case None => // do nothing
          case Some(0) => queue.synchronized(queue.wait)
          case Some(time: Long) => queue.synchronized(queue.wait(time))
        }
      }
    }
  }

  private case class JobHandleImpl(job: Job, schedule: JobSchedule, group: GroupActor) extends JobHandle

  private case class Scheduled(handle: JobHandleImpl, nextRun: Long, lastRun: Long) extends Ordered[Scheduled] {
    def compare(that: Scheduled): Int = {
      if (this.nextRun < that.nextRun) +1
      else if (this.nextRun > that.nextRun) -1
      else if (this.lastRun < that.lastRun) +1
      else if (this.lastRun > that.lastRun) -1
      else 0
    }
  }
}

trait Clock {
  def now: Long
}

trait Job {
  def execute()
}

trait JobHandle {
  def job: Job
  def schedule: JobSchedule
}

object Job {
  def apply(f: => Any) = new Job {
    def execute() = f
  }
}

object SystemClock extends Clock {
  def now = System.currentTimeMillis
}

abstract class JobSchedule {
  def firstRun(currentTime: Long): Long
  def nextRun(currentTime: Long): Option[Long]
}

object JobSchedule {
  def once(delay: Long): JobSchedule =
    IntervalJobSchedule(delay, 0, false)

  def once(when: Date): JobSchedule = OnceSchedule(when.getTime)

  def repeat(interval: Long): JobSchedule =
    IntervalJobSchedule(interval, interval, true)

  def repeat(delay: Long, interval: Long): JobSchedule =
    IntervalJobSchedule(delay, interval, true)

  def cron(cronExpression: String) = CronParser.parseCronExpression(cronExpression)
}

case class OnceSchedule(when: Long) extends JobSchedule {
  def firstRun(currentTime: Long) = when
  def nextRun(currentTime: Long) = None
}

case class IntervalJobSchedule(delay: Long, interval: Long, repeating: Boolean) extends JobSchedule {
  def firstRun(currentTime: Long) = currentTime + delay
  def nextRun(currentTime: Long) = if (repeating) Some(currentTime + interval) else None
}

case class CronJobSchedule(
  seconds: List[Int],
  minutes: List[Int],
  hours: List[Int],
  daysOfMonth: List[Int],
  months: List[Int],
  daysOfWeek: List[Int],
  year: Option[Int]) extends JobSchedule
{
  import CronJobSchedule._

  def firstRun(currentTime: Long) = nextRun(currentTime).get

  def nextRun(currentTime: Long): Option[Long] = {
    val now = Calendar.getInstance()
    now.setTimeInMillis(currentTime)
    now.set(Calendar.MILLISECOND, 0)
    var alarm = now.clone.asInstanceOf[Calendar]
    var current = alarm.get(Calendar.SECOND)
    var offset = 0
    offset = getOffsetToNext(current, minSecond, maxSecond, seconds)
    alarm.add(Calendar.SECOND, offset)
    current = alarm.get(Calendar.MINUTE)
    offset = getOffsetToNextOrEqual(current, minMinute, maxMinute, minutes)
    alarm.add(Calendar.MINUTE, offset)
    current = alarm.get(Calendar.HOUR_OF_DAY)  // (as updated by minute shift)
    offset = getOffsetToNextOrEqual(current, minHour, maxHour, hours)
    alarm.add(Calendar.HOUR_OF_DAY, offset)
    if (daysOfMonth(0) != -1 && daysOfWeek(0) != -1) {
      val dayOfWeekAlarm = alarm.clone.asInstanceOf[Calendar]
      updateDayOfWeekAndMonth(dayOfWeekAlarm)
      val dayOfMonthAlarm = alarm.clone.asInstanceOf[Calendar]
      updateDayOfMonthAndMonth(dayOfMonthAlarm)
      if (dayOfMonthAlarm.getTime().getTime() < dayOfWeekAlarm.getTime().getTime()) {
        alarm = dayOfMonthAlarm
      }
      else {
        alarm = dayOfWeekAlarm
      }
    }
    else if (daysOfWeek(0) != -1) { // only dayOfWeek is restricted
      updateDayOfWeekAndMonth(alarm)
    }
    else if (daysOfMonth(0) != -1) { // only dayOfMonth is restricted
      updateDayOfMonthAndMonth(alarm)
    }
    Some(alarm.getTimeInMillis)
  }

  private def updateDayOfMonthAndMonth(alarm: Calendar) {
    var currentMonth = alarm.get(Calendar.MONTH)
    var currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH)
    var offset = 0
    while (!months.contains(currentMonth) || !daysOfMonth.contains(currentDayOfMonth)) {
      if (!months.contains(currentMonth)) {
        offset = getOffsetToNextOrEqual(currentMonth, minMonth, maxMonth, months)
        alarm.add(Calendar.MONTH, offset)
        alarm.set(Calendar.DAY_OF_MONTH, 1)
        currentDayOfMonth = 1
      }
      if (!daysOfMonth.contains(currentDayOfMonth)) {
        val maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH)
        offset = getOffsetToNextOrEqual(currentDayOfMonth, minDayOfMonth, maxDayOfMonth, daysOfMonth)
        alarm.add(Calendar.DAY_OF_MONTH, offset)
      }
      currentMonth = alarm.get(Calendar.MONTH)
      currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH)
    }
  }

  private def updateDayOfWeekAndMonth(alarm: Calendar) {
    var currentMonth = alarm.get(Calendar.MONTH)
    var currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK)
    var offset = 0
    while (!months.contains(currentMonth) || !daysOfWeek.contains(currentDayOfWeek)) {
      if (!months.contains(currentMonth)) {
        offset = getOffsetToNextOrEqual(currentMonth, minMonth, maxMonth, months)
        alarm.add(Calendar.MONTH, offset)
        alarm.set(Calendar.DAY_OF_MONTH, 1)
        currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK)
      }
      if (!daysOfWeek.contains(currentDayOfWeek)) {
        offset = getOffsetToNextOrEqual(currentDayOfWeek, minDayOfWeek, maxDayOfWeek, daysOfWeek)
        alarm.add(Calendar.DAY_OF_YEAR, offset)
      }
      currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK)
      currentMonth = alarm.get(Calendar.MONTH)
    }
  }

  private def getOffsetToNext(current: Int, min: Int, max: Int, values: List[Int]): Int = {
    if (values(0) == -1) {
      1
    }
    else {
      if (current >= values.last) {
        val next = values(0)
        (max - current + 1) + (next - min)
      }
      else { // current < max(values) -- find next valid value after current
        values.find(_ > current).get - current
      } // end current < max(values)
    }
  }

  private def getOffsetToNextOrEqual(current: Int, min: Int, max: Int, values: List[Int]): Int = {
    if (values(0) == -1 || values.contains(current)) {
      0
    }
    else {
      val safeValues = values.filter(_ <= max)
      if (current > safeValues.last) {
        val next = safeValues(0)
        (max-current+1) + (next-min)
      }
      else { // current <= max(values) -- find next valid value
        safeValues.find(_ > current).get - current
      } // end current <= max(values)
    }
  }
}

private object CronJobSchedule {
  private val minSecond = 0
  private val maxSecond = 59
  private val minMinute = 0
  private val maxMinute = 59
  private val minHour = 0
  private val maxHour = 23
  private val minDayOfMonth = 1
  private val minMonth = 0
  private val maxMonth = 11
  private val minDayOfWeek = 1
  private val maxDayOfWeek = 7
}

object CronParser {
  private val all = List(-1)

  def parseCronExpression(cronExpression: String): CronJobSchedule = {
    val cronParts = cronExpression.split(" ");
    if (cronParts.length < 6 || cronParts.length > 7) {
      throw new IllegalArgumentException("invalid cron expression: " + cronExpression)
    }
    val seconds = parseCronUnit(cronParts(0), 0, 59)
    val minutes = parseCronUnit(cronParts(1), 0, 59)
    val hours = parseCronUnit(cronParts(2), 0, 23)
    val daysOfMonth = parseCronUnit(cronParts(3), 1, 31)
    val months = parseCronUnit(cronParts(4), 1, 12)
    val daysOfWeek = parseCronUnit(cronParts(5), 1, 7)
    val year = if (cronParts.length == 7) Some(parseInt(cronParts(6), 1970, 2099)) else None
    new CronJobSchedule(seconds, minutes, hours, daysOfMonth, months, daysOfWeek, year)
  }

  private def parseCronUnit(unit: String, min: Int, max: Int): List[Int] = {
    if (unit.equals("*") || unit.equals("?")) {
      all
    }
    else {
      val slash = unit.indexOf('/')
      val values = new ListBuffer[Int]
      if (slash != -1) {
        var offset = parseInt(unit.substring(0, slash), min, max)
        val interval = parseInt(unit.substring(slash + 1), 1, max)

        while (offset <= max) {
           values += offset
           offset += interval
        }
      }
      else {
        val ranges = unit.split(",")
        for (range <- unit.split(",")) {
          val dash = range.indexOf('-')
          if (dash == -1) {
            values += parseInt(range, min, max)
          }
          else {
            val start = parseInt(range.substring(0, dash), min, max)
            val end = parseInt(range.substring(dash + 1), min, max)
            start.to(end).foreach(values += _)
          }
        }
      }
      values.toList
    }
  }

  private def parseInt(str: String, min: Int, max: Int): Int = {
    try {
      val num = str.toInt
      if (num < min || num > max) {
        throw new NumberFormatException
      }
      else {
        num
      }
    }
    catch {
      case e: NumberFormatException =>
        throw new IllegalArgumentException("Invalid value in cron expresion: '" + str + "', expecting number in the range " + min + "-" + max)
    }
  }
}

protected object GroupActor {
}

protected class GroupActor(groupName: String) extends Actor {

  def act() {
    loop {
      receive {
        case RunJob(job) =>
          val originalThreadName = Thread.currentThread.getName
          Thread.currentThread.setName(job.toString)
          try {
            job.execute()
          }
          catch {
            case _ => {}
          }
          Thread.currentThread.setName(originalThreadName)

        case msg => {}
      }
    }
  }
}

protected case class RunJob(job: Job)