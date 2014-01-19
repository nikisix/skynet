package com.ign.hackweek.skynet.utils

import twitter4j._
import twitter4j.conf.ConfigurationBuilder

trait TwitterSearch {
  def search(query: String ): QueryResult = {
    try {
      //todo if factory is null call a builder function that initiates factory with oauth creds
      val consumerKey = "wkzcX2MqCkyjRdoSzIlYA"
      val consumerSecret = "Bq84T8IbJeLyBFNsuRp8zgMkL3WEihCEyigHVvGr5k"
      val accessToken = "43985865-pUx7jvuyJZJKGDFAqaMlB7K8DJ30d1pCtqrx8aHXY"
      val accessTokenSecret = "L1lOKurkNP0ohhS29e9xif8Tguc9rWuPacYExietgrAGM"

      val cb : ConfigurationBuilder = new ConfigurationBuilder
      cb.setOAuthConsumerKey(consumerKey)
      cb.setOAuthConsumerSecret(consumerSecret)
      cb.setOAuthAccessToken(accessToken)
      cb.setOAuthAccessTokenSecret(accessTokenSecret)

      val factory: TwitterFactory = new TwitterFactory(cb.build)
      //todo put oauth token in call to getInstance
      val twitter: Twitter = factory.getInstance()
      val twQuery: Query = new Query(query)
      twQuery.setResultType("recent")
//      twQuery.setRpp(50)
      val result: QueryResult = twitter.search(twQuery)
      if (result != null && result.getTweets != null && result.getTweets.size > 0)
        return result
    } catch {
      case _ => {}
    }
    return null
  }
}
