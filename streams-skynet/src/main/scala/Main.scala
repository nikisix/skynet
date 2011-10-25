package com.ign.hackweek.skynet.streams

import twitter4j.{TwitterStream,TwitterStreamFactory}
import twitter4j.auth.{AccessToken}
import twitter4j.conf.{Configuration,ConfigurationBuilder}

object Main {
	def main(args: Array[String]) {
		val builder: ConfigurationBuilder = new ConfigurationBuilder
		val config: Configuration = builder.setUser("skynetign").setPassword("abcd1234").build
		val streamFactory: TwitterStreamFactory = new TwitterStreamFactory(config)
		val stream: TwitterStream = streamFactory.getInstance
		stream.addListener(TwitterListener)
		stream.sample
		//Config.streams.foreach(println)
	}
}
