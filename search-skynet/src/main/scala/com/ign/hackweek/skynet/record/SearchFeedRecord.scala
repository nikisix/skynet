package com.ign.hackweek.skynet.record

import com.ign.hackweek.skynet.model.SearchFeed
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.common.Loggable
import net.liftweb.record.field.{StringField,BooleanField}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.json.JsonDSL._

class SearchFeedRecord private() extends MongoRecord[SearchFeedRecord] with ObjectIdPk[SearchFeedRecord] {
  def meta = SearchFeedRecord

  object name extends StringField(this, 64)

  object query extends StringField(this, 1024)

  object parent extends StringField(this, 64)

  object enabled extends BooleanField(this, true)

  def toModel = {
    SearchFeed(this.name.value, this.query.value, this.parent.value, this.enabled.value)
  }
}

object SearchFeedRecord extends SearchFeedRecord with MongoMetaRecord[SearchFeedRecord] with Loggable {
  def findByName(name: String): Option[SearchFeedRecord] = SearchFeedRecord.find(("name" -> name))
}
