package com.neurone.neuroneconector.endpoints

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import com.neurone.neuroneconector.services.service._
import com.neurone.neuroneconector.services.serviceAll._
import com.twitter.util.{FuturePool, Future}
import java.util.concurrent._
import scala.concurrent._

/**Endpoints declaration**/

// Singleton object to provides endpoints functions
package object endpoints {

  /*Package*/

  //Method to package Tuple[String,Double] to circe.Json object
  def packageResult(username: String, metricName: String, result: Double) = {
    val json: Json = Json.obj(
      ("username", Json.fromString(username)),
      (metricName, Json.fromDoubleOrNull(result)),
      ("metric", Json.fromString(metricName))
    )
    json
  }
  // Function to package a Seq[Tuple2[String,Double]] to circe.Json object
  val packageResults =
    (results: Seq[Tuple2[String, Double]], metricName: String) => {
      val jsonResults: Seq[Json] =
        results.map(result => packageResult(result._1, metricName, result._2))
      jsonResults
    }

  // This value contains the query params ti and tf used in all endpoints
  val standardParams = paramOption[Int]("ti") :: paramOption[Int]("tf")
  // This value add the username param
  val standardParamsForOneUser = path[String] :: standardParams

  /*Endpoints declararion*/

  //Get->/multiple/:username?ti=12&tf=120&relevants=4&limitTime=40&metrics=totalcover&metrics=recall
  //Endpoint to request multiple metrics for a specific user
  val getMultipleMetricsByUser = get(
    "multiple" :: standardParamsForOneUser ::
      paramOption[Double]("relevants") :: paramOption[Int]("limitTime") ::
      params[String]("metrics")
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        relevants: Option[Double],
        limitTime: Option[Int],
        metrics: Seq[String]
    ) =>
      // println(username)
      val results =
        getMultpleMetricsService(ti)(tf)(username)(relevants.getOrElse(3.0))(
          limitTime.getOrElse(30)
        )(metrics)
      val jsonResults: Seq[Json] =
        results.zipWithIndex.map(
          result =>
            packageResult(result._1._1, metrics(result._2), result._1._2)
        )
      jsonResults.map(metric => println(metric))
      Ok(jsonResults)
  }

  //TotalCover

  // Get->/totalcover/:username?ti=12&tf=132
  // Endpoint to get totalcover for one participant
  val getTotalCoverEndpoint = get(
    "totalcover" :: standardParamsForOneUser
  ) { (username: String, ti: Option[Int], tf: Option[Int]) =>
    FuturePool.unboundedPool {
      val totalCover: Tuple2[String, Double] =
        getTotalCoverService(ti)(tf)(username)
      val jsonResult: Json =
        packageResult(username, "totalcover", totalCover._2)
      Ok(jsonResult)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  // Get->/totalcover?ti=1&tf=10
  // Endpoint to get totalcover for all participants
  val getTotalCoverForAllEndpoint =
    get("totalcover" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getTotalCoverServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "totalcover")
        Ok(jsonResults)
      }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  // BmRelevant

  //Get->/bmrelevant/:username?ti=1&tf=10
  //Endpoint to get BmRelevant for one participant
  val getBmRelevantEndpoint = get(
    "bmrelevant" :: standardParamsForOneUser
  ) { (username: String, ti: Option[Int], tf: Option[Int]) =>
    FuturePool.unboundedPool {
      val bmrelevant: Tuple2[String, Double] =
        getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)
      val jsonResult = packageResult(username, "bmrelevant", bmrelevant._2)
      Ok(jsonResult)
    }

  }
  //Get->/bmrelevant?ti=1&tf=10
  //Endpoint to get BmRelevant for all participants
  val getBmRelevantForAllEndpoint =
    get("bmrelevant" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getRelevantOrActiveBmServiceForAll(ti, tf, Option(true))
        val jsonResults = packageResults(results, "bmrelevant")
        Ok(jsonResults)
      }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  // Precision

  //Get->/precision/:username?ti=1&tf=10
  //Endpoint to get Precision for one participant
  val getPrecisionEndpoint = get(
    "precision" :: standardParamsForOneUser
  ) { (username: String, ti: Option[Int], tf: Option[Int]) =>
    FuturePool.unboundedPool {
      val precision: Tuple2[String, Double] =
        getPrecisionService(ti)(tf)(username)
      val jsonResult = packageResult(username, "precision", precision._2)
      Ok(jsonResult)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/precision?ti=1&tf=10
  //Endpoint to get precision for all participants
  val getPrecisionForAllEndpoint =
    get("precision" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getPrecisionServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "precision")
        Ok(jsonResults)
      }

    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //Recall

  //Get->/recall/:username?ti=1&tf=10
  //Endpoint to get Recall for one participant
  val getRecallEndpoint = get(
    "recall" :: standardParamsForOneUser :: paramOption[Double]("relevants")
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        relevants: Option[Double]
    ) =>
      FuturePool.unboundedPool {
        val recall: Tuple2[String, Double] =
          getRecallService(ti)(tf)(relevants.getOrElse(3.0))(username)
        val jsonResult = packageResult(username, "recall", recall._2)
        Ok(jsonResult)

      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/recall?ti=1&tf=10
  //Endpoint to get Recall for all participants
  val getRecallForAllEndpoint = get(
    "recall" :: standardParams :: paramOption[Double]("relevants")
  ) { (ti: Option[Int], tf: Option[Int], relevants: Option[Double]) =>
    FuturePool.unboundedPool {
      val results = getRecallServiceForAll(relevants.getOrElse(3.0), ti, tf)
      val jsonResults = packageResults(results, "recall")
      Ok(jsonResults)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }
  //F1-Fscore

  //Get->/f1/:username?ti=1&tf=10
  //Endpoint to get F1 for one participants
  val getF1Endpoint = get(
    "f1" :: standardParamsForOneUser :: paramOption[Double]("relevants")
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        relevants: Option[Double]
    ) =>
      FuturePool.unboundedPool {
        val f1: Tuple2[String, Double] =
          getFScoreService(ti)(tf)(relevants.getOrElse(3.0))(username)
        val jsonResult = packageResult(username, "f1", f1._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/f1?ti=1&tf=10
  //Endpoint to get F1 for all participants
  val getF1ForAllEndpoint = get(
    "f1" :: standardParams :: paramOption[Double]("relevants")
  ) { (ti: Option[Int], tf: Option[Int], relevants: Option[Double]) =>
    FuturePool.unboundedPool {
      val results = getF1ServiceForAll(relevants.getOrElse(3.0), ti, tf)
      val jsonResults = packageResults(results, "f1")
      Ok(jsonResults)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  // UsfCover

  //Get->/usfcover/:username?ti=1&tf=10
  //Endpoint to get UsfCover for one participant
  val getUsfCoverEndpoint = get(
    "usfcover" :: standardParamsForOneUser :: paramOption[Int]("limitTime")
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        limitTime: Option[Int]
    ) =>
      FuturePool.unboundedPool {
        val usfcover =
          getUsfCoverService(ti)(tf)(limitTime.getOrElse(30))(username)
        val jsonResult = packageResult(username, "usfcover", usfcover._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/usfcover?ti=1&tf=10
  //Endpoint to get UsfCover for all participants
  val getUsfCoverForAllEndpoint = get(
    "usfcover" :: standardParams :: paramOption[Int]("limitTime")
  ) { (ti: Option[Int], tf: Option[Int], limitTime: Option[Int]) =>
    FuturePool.unboundedPool {
      val results = getUsfCoverServiceForAll(limitTime.getOrElse(30), ti, tf)
      val jsonResults = packageResults(results, "usfcover")
      Ok(jsonResults)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //NumQueries

  //Get->/numqueries/:username?ti=1&tf=10
  //Endpoint to get NumQueries for one participant
  val getNumQueriesEndpoint = get("numqueries" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val numQueries = getNumQueryService(ti)(tf)(username)
        val jsonResult = packageResult(username, "numqueries", numQueries._2)
        Ok(jsonResult)

      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/numqueries?ti=1&tf=10
  //Endpoint to get NumQueries for all participants
  val getNumQueriesForAllEndpoint =
    get("numqueries" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getNumQueriesServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "numqueries")
        Ok(jsonResults)

      }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //CoverageEffectiveness

  //Get->/ceffectiveness/:username?ti=1&tf=10
  //Endpoint to get CoverageEffectiveness for one participant
  val getCoverageEffectivenessEndpoint = get(
    "ceffectiveness" :: standardParamsForOneUser :: paramOption[Int](
      "limitTime"
    )
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        limitTime: Option[Int]
    ) =>
      FuturePool.unboundedPool {
        val coverageEffectiveness = getCoverageEffectivenessService(ti)(tf)(
          limitTime.getOrElse(30)
        )(username)
        val jsonResult = packageResult(
          username,
          "ceffectiveness",
          coverageEffectiveness._2
        )
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/ceffectiveness?ti=1&tf=10
  //Endpoint to get CoverageEffectiveness for all participants
  val getCoverageEffectivenessForAllEndpoint =
    get("ceffectiveness" :: standardParams :: paramOption[Int]("limitTime")) {
      (
          ti: Option[Int],
          tf: Option[Int],
          limitTime: Option[Int]
      ) =>
        FuturePool.unboundedPool {
          val results = getCoverageEffectivenessServiceForAll(
            ti,
            tf,
            limitTime.getOrElse(30)
          )
          val jsonResults = packageResults(results, "ceffectiveness")
          Ok(jsonResults)
        }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //QueryEffectiveness

  //Get->/qeffectiveness/:username?ti=1&tf=10
  //Endpoint to get QueryEffectiveness for one participant
  val getQueryEffectivenessEndpoint = get(
    "qeffectiveness" :: standardParamsForOneUser :: paramOption[Int](
      "limitTime"
    )
  ) {
    (
        username: String,
        ti: Option[Int],
        tf: Option[Int],
        limitTime: Option[Int]
    ) =>
      FuturePool.unboundedPool {
        val queryEffectiveness = getQueryEffectivenessService(ti)(tf)(
          limitTime.getOrElse(30)
        )(username)
        val jsonResult = packageResult(
          username,
          "qeffectiveness",
          queryEffectiveness._2
        )
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/qeffectiveness?ti=1&tf=10
  //Endpoint to get QueryEffectiveness for all participants
  val getQueryEffectivenessForAllEndpoint = get(
    "qeffectiveness" :: standardParams :: paramOption[Int]("limitTime")
  ) {
    (
        ti: Option[Int],
        tf: Option[Int],
        limitTime: Option[Int]
    ) =>
      FuturePool.unboundedPool {
        val results =
          getQueryEffectivenessServiceForAll(ti, tf, limitTime.getOrElse(30))
        val jsonResults = packageResults(results, "qeffectiveness")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //ActiveBm

  //Get->/activebm/:username?ti=1&tf=10
  //Endpoint to get ActiveBm for one participant
  val getActiveBmEndpoint = get("activebm" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val activeBm: Tuple2[String, Double] =
          getActiveOrRelevantBmService(ti)(tf)(None)(username)
        val jsonResult = packageResult(username, "activebm", activeBm._2)
        Ok(jsonResult)

      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/activebm?ti=1&tf=10
  //Endpoint to get ActiveBm for all participants
  val getActiveBmForAllEndpoint = get("activebm" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getRelevantOrActiveBmServiceForAll(ti, tf, None)
        val jsonResults = packageResults(results, "activebm")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  // SearchScore

  //Get->/score/:username?ti=1&tf=10
  //Endpoint to get SearchScore for one participant
  val getSearchScoreEndpoint = get("score" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val searchScore = getSearchScoreService(ti)(tf)(username)
        val jsonResult = packageResult(username, "score", searchScore._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/score?ti=1&tf=10
  //Endpoint to get SearchScore for all participants
  val getSearchScoreForAllEndpoint = get("score" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getSearchScoreServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "score")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  // TotalPagestay

  //Get->/pagestay/:username?ti=1&tf=10
  //Endpoint to get TotalPagestay for one participant
  val getTotalPageStayEndpoint = get("pagestay" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val totalPageStay = getTotalPageStayService(ti)(tf)(username)
        val jsonResult =
          packageResult(username, "pagestay", totalPageStay._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/pagestay?ti=1&tf=10
  //Endpoint to get TotalPagestay for all participants
  val getTotalPageStayForAllEndpoint = get("pagestay" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getTotalPageStayServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "pagestay")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Mousecliks

  //Get->/mouseclicks/:username?ti=1&tf=10
  //Endpoint to get Mousecliks for one participant
  val getMouseClicksEndpoint = get("mouseclicks" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val mouseClicks = getMouseClicksService(ti)(tf)(username)
        val jsonResult = packageResult(username, "mouseclicks", mouseClicks._2)
        Ok(mouseClicks)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/mouseclicks?ti=1&tf=10
  //Endpoint to get Mousecliks for all participants
  val getMouseClicksForAllEndpoint =
    get("mouseclicks" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getMouseclicksServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "mouseclicks")
        Ok(jsonResults)
      }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  // Mousecoordinates

  //Get->/mousecoordinates/:username?ti=1&tf=10
  //Endpoint to get Mousecoordinates for one participant
  val getMouseCoordinatesEndpoint =
    get("mousecoordinates" :: standardParamsForOneUser) {
      (username: String, ti: Option[Int], tf: Option[Int]) =>
        FuturePool.unboundedPool {
          val mouseCoordinates = getMouseCoordinatesService(ti)(tf)(username)
          val jsonResult =
            packageResult(username, "mousecoordinates", mouseCoordinates._2)
          Ok(jsonResult)
        }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //Get->/mousecoordinates?ti=1&tf=10
  //Endpoint to get Mousecoordinates for all participants
  val getMouseCoordinatesForAllEndpoint =
    get("mousecoordinates" :: standardParams) {
      (ti: Option[Int], tf: Option[Int]) =>
        //println("mousecordinates")
        FuturePool.unboundedPool {
          val results = getMouseCoordinatesServiceForAll(ti, tf)
          val jsonResults = packageResults(results, "mousecoordinates")
          //println("terminado mousecoordinates")
          Ok(jsonResults)
        }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  // ScrollMoves

  //Get->/scrollmoves/:username?ti=1&tf=10
  //Endpoint to get ScrollMoves for one participant
  val getScrollMovesEndpoint = get("scrollmoves" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val scrollMoves = getScrollMovesService(ti)(tf)(username)
        val jsonResult = packageResult(username, "scrollmoves", scrollMoves._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/scrollmoves?ti=1&tf=10
  //Endpoint to get ScrollMoves for all participants
  val getScrollMovesForAllEndpoint =
    get("scrollmoves" :: standardParams) { (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getScrollMovesServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "scrollmoves")
        Ok(jsonResults)
      }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //TotalModQuery

  //Get->/modquery/:username?ti=1&tf=10
  //Endpoint to get TotalModQuery for one participant
  val getTotalModQueryEndpoint = get("modquery" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val totalModQuery = getTotalModQueryService(ti)(tf)(username)
        val jsonResult =
          packageResult(username, "totalmodquery", totalModQuery._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/modquery?ti=1&tf=10
  //Endpoint to get TotalModQuery for all participants
  val getTotalModQueryForAllEndpoint = get("modquery" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getTotalModQueryServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "totalmodquery")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }
  // WritingTime

  //Get->/writingtime/:username?ti=1&tf=10
  //Endpoint to get WritingTime for one participant
  val getWritingTimeEndpoint = get("writingtime" :: standardParamsForOneUser) {
    (username: String, ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val writingTime = getWritingTimeService(ti)(tf)(username)
        val jsonResult = packageResult(username, "writingtime", writingTime._2)
        Ok(jsonResult)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //Get->/writingtime?ti=1&tf=10
  //Endpoint to get WritingTime for all participants
  val getWritingTimeForAllEndpoint = get("writingtime" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getWritingTimeServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "writingtime")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //QueryEntropy

  //Get->/entropy/:username?ti=1&tf=10
  //Endpoint to get QueryEntropy for one participant
  val getAverageQueryEntropyEndpoint =
    get("entropy" :: standardParamsForOneUser) {
      (username: String, ti: Option[Int], tf: Option[Int]) =>
        FuturePool.unboundedPool {
          val averageQueryEntropy =
            getAverageQueryEntropyService(ti)(tf)(username)
          val jsonResult =
            packageResult(username, "entropy", averageQueryEntropy._2)
          Ok(jsonResult)
        }
    }.handle {
      case e: Error.NotPresent => BadRequest(e)
    }

  //Get->/entropy?ti=1&tf=10
  //Endpoint to get QueryEntropy for all participant
  val getAverageQueryEntropyForAllEndpoint = get("entropy" :: standardParams) {
    (ti: Option[Int], tf: Option[Int]) =>
      FuturePool.unboundedPool {
        val results = getAverageQueryEntropyServiceForAll(ti, tf)
        val jsonResults = packageResults(results, "entropy")
        Ok(jsonResults)
      }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
  }

  //InitStageTime

  //Get->/init:/username
  //Endpoint to get InitStageTime for one participant
  val getStageTimeEndpoint = get("init" :: path[String]) { (username: String) =>
    FuturePool.unboundedPool {
      val init = getStageTimeService(username)
      val jsonResult = packageResult(username, "inittime", init._2)
      Ok(jsonResult)
    }
  }.handle {
    case e: Error.NotPresent => BadRequest(e)
    
  }

  // This value constains all endpoints
  val combined = getMultipleMetricsByUser :+: getTotalCoverEndpoint :+: getTotalCoverForAllEndpoint :+:
    getBmRelevantEndpoint :+: getBmRelevantForAllEndpoint :+:
    getPrecisionEndpoint :+: getPrecisionForAllEndpoint :+:
    getRecallEndpoint :+: getRecallForAllEndpoint :+:
    getF1Endpoint :+: getF1ForAllEndpoint :+:
    getUsfCoverEndpoint :+: getUsfCoverForAllEndpoint :+:
    getNumQueriesEndpoint :+: getNumQueriesForAllEndpoint :+:
    getCoverageEffectivenessEndpoint :+: getCoverageEffectivenessForAllEndpoint :+:
    getQueryEffectivenessEndpoint :+: getQueryEffectivenessForAllEndpoint :+:
    getActiveBmEndpoint :+: getActiveBmForAllEndpoint :+:
    getSearchScoreEndpoint :+: getSearchScoreForAllEndpoint :+:
    getTotalPageStayEndpoint :+: getTotalPageStayForAllEndpoint :+:
    getMouseClicksEndpoint :+: getMouseClicksForAllEndpoint :+:
    getMouseCoordinatesEndpoint :+: getMouseCoordinatesForAllEndpoint :+:
    getScrollMovesEndpoint :+: getScrollMovesForAllEndpoint :+:
    getWritingTimeEndpoint :+: getWritingTimeForAllEndpoint :+:
    getTotalModQueryEndpoint :+: getTotalModQueryForAllEndpoint :+:
    getAverageQueryEntropyEndpoint :+: getAverageQueryEntropyForAllEndpoint :+:
    getStageTimeEndpoint

}
