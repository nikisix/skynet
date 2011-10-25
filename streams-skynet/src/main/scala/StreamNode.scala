package com.ign.hackweek.skynet.streams

import twitter4j.{TwitterStream,TwitterStreamFactory,FilterQuery}
import twitter4j.conf.{Configuration,ConfigurationBuilder}

class StreamNode(name: String, filter: FilterQuery) {
	val builder: ConfigurationBuilder = new ConfigurationBuilder
	val config: Configuration = builder.setUser(Config.credentials._1).setPassword(Config.credentials._2).build
	val factory: TwitterStreamFactory = new TwitterStreamFactory(config)
	val stream: TwitterStream = factory.getInstance
	stream.addListener(new StreamListener(name))

	def start = stream.filter(filter)
}
