package bootstrap.liftweb

import net.liftweb._
import mongodb._
import util._
import http._
import com.ign.hackweek.skynet.streams._

import com.mongodb.ServerAddress
import com.ign.hackweek.skynet.resource.StreamsResource

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    LiftRules.addToPackages("com.ign.hackweek.skynet.streams")
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    //
    LiftRules.dispatch.append(StreamsResource)
    // stateless REST handlers
    LiftRules.statelessDispatchTable.append(StreamsResource)
  }
}
