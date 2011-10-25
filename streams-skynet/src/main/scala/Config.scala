package com.ign.hackweek.skynet.streams

import twitter4j.{FilterQuery}

object Config {
	val environment = System.getProperty("environment","dev")

	val credentials = environment match {
			case _ => ("skynetign","abcd1234")
		}
	val streams = environment match {
			case _ => Map(
				"SF_Keys" -> new FilterQuery(0,null,
					Array("play","game"), 
					Array(Array(-122.75,36.8),Array(-121.75,37.8))),
				"NY_Keys" -> new FilterQuery(0,null,
					Array("playing","gaming"),
					Array(Array(-74.0,40.0),Array(-73.0,41.0)))
				) 
		}
}
