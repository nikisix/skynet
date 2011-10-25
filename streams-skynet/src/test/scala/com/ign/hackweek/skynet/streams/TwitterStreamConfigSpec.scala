package com.ign.hackweek.skynet.streams

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner

import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class TwitterStreamConfigSpec extends Spec with MustMatchers{

  /**
   * val configuration = TwitterStreamConfig("a label")("skynetign","abcd1234")(
    FilterQuery(0, null,
					Array("play","game"),
					Array(Array(-122.75,36.8), Array(-121.75,37.8))),

    WhiteListProcessor( "play" :: "game" :: Nil ) :: Nil,

    MessageQueue("raw.queue")

  ) :: Nil

   */

  describe( "Twitter Stream Config creation..." ){

    it(" should hold the basic set configuration ") {

      val conf = TwitterStreamConfig.configuration

      conf.head.label must be === "a label"
      conf.head.credentials._1 must be === "skynetign"
      conf.head.credentials._2 must be === "abcd1234"
      conf.head.filterQuery must not be(null)
      conf.head.postProcs.isEmpty must not be(true)
      conf.head.queue must not be(null)

    }
  }
}