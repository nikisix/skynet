package com.ign.hackweek.skynet.resource

import net.liftweb.http.rest._
import net.liftweb.json._
import com.ign.hackweek.skynet.service.SearchService

object SearchResource extends RestHelper {

  serve("v1" / "search"   prefix {

    case "start" :: Nil JsonGet _ => Extraction.decompose( SearchService.start )

    case "stop" :: Nil JsonGet _ => Extraction.decompose( SearchService.stop )

    case "new" :: Nil JsonGet _ => Extraction.decompose( SearchService.newFeed )

    case "status" :: Nil JsonGet _ => Extraction.decompose( SearchService.status )

  })

}

