package com.ign.hackweek.skynet.record

import net.liftweb.mongodb.{ObjectIdSerializer, JsonObjectMeta, JsonObject}
import twitter4j.Tweet
import java.util.{Date, Calendar}
import net.liftweb.record.field._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.common.Loggable

class TweetTag private () extends BsonRecord[TweetTag] {
  def meta = TweetTag
  object name extends StringField(this, 256)
  object count extends IntField(this, 0)
}
object TweetTag extends TweetTag with BsonMetaRecord[TweetTag]

class TweetObject private () extends BsonRecord[TweetObject] {
  def meta = TweetObject
  object id extends StringField(this, 64)
  object text extends StringField(this, 256)
  object geoLongitude extends DoubleField(this, 0)
  object geoLatitude extends DoubleField(this, 0)
  object created extends DateTimeField(this, Calendar.getInstance)
  object fromUserId extends StringField(this,32)
  object toUserId extends StringField(this,32)
  object tags extends BsonRecordListField(this, TweetTag)
}
object TweetObject extends TweetObject with BsonMetaRecord[TweetObject] with Loggable {
  def createFromTweet(tweet: Tweet): TweetObject = {
    val tweetTags = tweet.getHashtagEntities.map(tag => TweetTag.createRecord.name(tag.getText).count(1)).toList
    val tweetDate = Calendar.getInstance
    tweetDate.setTime(tweet.getCreatedAt)
    val newTweet = createRecord
    newTweet.id(tweet.getId.toString)
    newTweet.text(tweet.getText)
    if (tweet.getGeoLocation != null)
      newTweet.geoLongitude(tweet.getGeoLocation.getLongitude)
    if (tweet.getGeoLocation != null)
      newTweet.geoLatitude(tweet.getGeoLocation.getLatitude)
    newTweet.created(tweetDate)
    newTweet.fromUserId(tweet.getFromUserId.toString)
    newTweet.toUserId(tweet.getToUserId.toString)
    newTweet.tags(tweetTags)
    newTweet
  }
}

class Trend private () extends BsonRecord[Trend] {
  def meta = Trend
  object name extends StringField(this, 64)
  object tags extends BsonRecordListField(this, TweetTag)
  object tweets extends BsonRecordListField(this, TweetObject)
}
object Trend extends Trend with BsonMetaRecord[Trend]

class TrendRecord private () extends MongoRecord[TrendRecord] with ObjectIdPk[TrendRecord] {
  def meta = TrendRecord
  object created extends DateTimeField(this, Calendar.getInstance)
  object timeFrame extends IntField(this, 0)
  object trends extends BsonRecordListField(this, Trend)
}

object TrendRecord extends TrendRecord with MongoMetaRecord[TrendRecord] {
  override def formats = super.formats + new ObjectIdSerializer
}
