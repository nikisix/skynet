package com.ign.hackweek.skynet.streams

import twitter4j.{TwitterStream,TwitterStreamFactory,FilterQuery}
import twitter4j.conf.{Configuration,ConfigurationBuilder}

class TwitterStreamNode(streamConfig:TwitterStreamConfig) {
	val builder: ConfigurationBuilder = new ConfigurationBuilder
	val config: Configuration = builder.setUser(streamConfig.credentials._1).setPassword(streamConfig.credentials._2).build
	val factory: TwitterStreamFactory = new TwitterStreamFactory(config)
	val stream: TwitterStream = factory.getInstance
  val listener: TwitterStreamListener = new TwitterStreamListener(streamConfig.label, streamConfig.queue, streamConfig.postProcs)

  stream.addListener(listener)

	def start = stream.filter(streamConfig.filterQuery)

  def stop = stream.shutdown()

  def status = listener.status

  def id = streamConfig.label

}
