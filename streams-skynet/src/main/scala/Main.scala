package com.ign.hackweek.skynet.stream

import twitter4j.{TwitterStream,TwitterStreamFactory}

object Main {
	def main(args: Array[String]) {
		TwitterStream stream = new TwitterStreamFactory(TwitterListener).getInstance
		stream.sample
		//Config.streams.foreach(println)
	}
}
