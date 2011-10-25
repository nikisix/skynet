package com.ign.hackweek.skynet.streams

import twitter4j.{Status,StatusListener}

object TwitterListener extends StatusListener {
	override def onStatus(status: Status) = println(status.getUser.getName + " : " + status.getText)
}
