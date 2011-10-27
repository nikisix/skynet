package com.ign.hackweek.skynet.resource

import net.liftweb.http.rest._
import net.liftweb.json._
import com.ign.hackweek.skynet.service.SearchService

object SearchResource extends RestHelper {

  serve("v1" / "service" prefix {

    case "start" :: Nil JsonGet _ => Extraction.decompose( SearchService.startJobs )

    case "stop" :: Nil JsonGet _ => Extraction.decompose( SearchService.stopJobs )

    case "register" :: Nil JsonPut json -> _ => Extraction.decompose( SearchService.registerFeed(json) )

    case "status" :: Nil JsonGet _ => Extraction.decompose( SearchService.status )

  })

}

