package com.ign.hackweek.skynet.resource

import net.liftweb.http.rest._
import net.liftweb.json._
import com.ign.hackweek.skynet.service.TrendsService

object TrendsResource extends RestHelper {

  serve("v1" / "service" prefix {

    case "start" :: Nil JsonGet _ => Extraction.decompose( TrendsService.startJobs )

    case "stop" :: Nil JsonGet _ => Extraction.decompose( TrendsService.stopJobs )

    case "status" :: Nil JsonGet _ => Extraction.decompose( TrendsService.status )

  })

}

