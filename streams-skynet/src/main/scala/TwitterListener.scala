package com.ign.hackweek.skynet.stream

import twitter4j.{Status,StatusListener}

object TwitterListener extends StatusListener {
	def onStatus(status: Status) = println(status.getUser.getName + " : " + status.getText)
}
