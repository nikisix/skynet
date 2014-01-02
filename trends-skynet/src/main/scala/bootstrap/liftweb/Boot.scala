package bootstrap.liftweb

import net.liftweb._
import mongodb._
import util._
import http._
import com.ign.hackweek.skynet.service._

import com.mongodb.ServerAddress
import com.ign.hackweek.skynet.resource.TrendsResource

class Boot {
  def boot {
    LiftRules.addToPackages("com.ign.hackweek.skynet.service")
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.dispatch.append(TrendsResource)
    LiftRules.statelessDispatchTable.append(TrendsResource)
    MongoDB.defineDb( DefaultMongoIdentifier,
        MongoAddress( MongoHost( "127.0.0.1", 27017), "skynet") )
  }
}
