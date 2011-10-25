package com.ign.hackweek.skynet.streams

object Config {
	val environment = System.getProperty("environment","dev")
	val streams = Map(
			"basic" -> Map(
				"uri" -> "xxx",
				"params" -> "yyy",
				"queue" -> "zzz"
				),
			"tag1" -> Map(
				"uri" -> "xxx",
				"params" -> "yyy",
				"queue" -> "zzz"
				)
			)
}
