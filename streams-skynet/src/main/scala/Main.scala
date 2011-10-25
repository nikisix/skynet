package com.ign.hackweek.skynet.streams


import twitter4j.{TwitterStream,TwitterStreamFactory}

object Main {
	def main(args: Array[String]) {
		TwitterStreamFactory.getSingleton.addListener(TwitterListener)
		//stream.sample()
		//Config.streams.foreach(println)
	}
}
