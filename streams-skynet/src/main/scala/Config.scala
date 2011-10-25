package com.ign.hackweek.skynet.stream

object Config {
	val environment = System.getProperty("environment","dev")
	val stream = Map(
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
