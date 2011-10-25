package com.ign.hackweek.skynet.streams

import twitter4j.{Status,StatusListener,StatusDeletionNotice}

class StreamListener (name: String) 
	extends StatusListener {
	override def onStatus(status: Status) = println("Node:" + this.name + "\n" + status.getUser.getName + " : " + status.getText)
	override def onScrubGeo(userId: Long, upToStatusId: Long) = println("onScrub")
	override def onTrackLimitationNotice(number: Int) = println("onTrack")
	override def onDeletionNotice(notice: StatusDeletionNotice) = println("onDeletion")
	override def onException(ex: Exception) = println("onException")
}
