package com.ign.hackweek.skynet.jobs

import twitter4j.Tweet
import collection.mutable.{HashMap, SynchronizedMap}

class TweetQueue {
  private val mapper = new HashMap[String, List[Tweet]] with SynchronizedMap[String, List[Tweet]]

  def add(name: String, items: List[Tweet]) = {
    if (mapper.contains(name)) {
      mapper += name -> items
    }
    else {
      var prevList = mapper.get(name).get
      prevList ++: items
      mapper.update(name,prevList)
    }

  }
}