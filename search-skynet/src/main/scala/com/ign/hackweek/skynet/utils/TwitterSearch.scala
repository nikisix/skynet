package com.ign.hackweek.skynet.utils

import twitter4j._

trait TwitterSearch {
  def search(query: String ): QueryResult = {
    val factory: TwitterFactory = new TwitterFactory
    val twitter: Twitter = factory.getInstance
    val twQuery: Query = new Query(query)
    twitter.search(twQuery)
  }
}
