package com.ign.hackweek.skynet.resource

import net.liftweb.http.rest._
import net.liftweb.json._
import com.ign.hackweek.skynet.service.StreamsService

object StreamsResource extends RestHelper {

  serve("v1" / "streams"   prefix {

    case "start" :: Nil JsonGet _ => Extraction.decompose( StreamsService.start )

    case "stop" :: Nil JsonGet _ => Extraction.decompose( StreamsService.stop )

    case "status" :: Nil JsonGet _ => Extraction.decompose( StreamsService.status )

  })

}

