package com.ign.hackweek.skynet.model

import com.ign.hackweek.skynet.record.SearchFeedRecord

case class SearchFeed(name: String, query: String, parent: String, enabled: Boolean)

object SearchFeed {
  def findByName(name: String): Option[SearchFeed] = {
    val result = SearchFeedRecord.findByName(name)
    if (result.isEmpty)
      None
    else
      Some(result.get.toModel)
  }

  def findAll: List[SearchFeed] = SearchFeedRecord.findAll.map(_.toModel).toList
}
