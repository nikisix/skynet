package com.ign.hackweek.skynet.utils

import twitter4j._

trait TwitterSearch {
  def search(query: String ): QueryResult = {
    try {
      val factory: TwitterFactory = new TwitterFactory
      val twitter: Twitter = factory.getInstance
      val twQuery: Query = new Query(query)
      twQuery.setResultType("recent")
      val result = twitter.search(twQuery)
      if (result != null && result.getTweets != null && result.getTweets.size > 0)
        return result
    } catch {
      case _ => {}
    }
    return null
  }
}
