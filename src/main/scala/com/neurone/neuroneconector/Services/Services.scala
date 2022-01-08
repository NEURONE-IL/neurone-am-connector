package com.neurone.neuroneconector.services

import com.neurone.neuroneconector.db.collections._
import com.neurone.neuroneconector.functions.mongoQueries._
import com.neurone.neuroneconector.functions.reductions._
import com.neurone.neuroneconector.functions.predicates._
import com.neurone.neuroneconector.functions.filters._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import com.neurone.neuroneconector.metrics.simple._
import org.mongodb.scala.bson.conversions.Bson
import com.neurone.neuroneconector.functions.transformation._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.finch.circe._

/** Services declaration* */
//Singleton object to provide metrics services
package object service {

  //Sercive to return a set of metric for a specific participant
  val getMultpleMetricsService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) =>
        (relevantDocuments: Double) =>
          (limitTime: Int) =>
            (metrics: Seq[String]) => {

              val results = metrics.map(metric => {

                metric match {
                  case "totalcover" => getTotalCoverService(ti)(tf)(username)
                  case "bmrelevant" =>
                    getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)
                  case "precision" => getPrecisionService(ti)(tf)(username)
                  case "recall" =>
                    getRecallService(ti)(tf)(relevantDocuments)(username)
                  case "f1" =>
                    getFScoreService(ti)(tf)(relevantDocuments)(username)
                  case "usfcover" =>
                    getUsfCoverService(ti)(tf)(limitTime)(username)
                  case "numqueries" => getNumQueryService(ti)(tf)(username)
                  case "ceffectiveness" =>
                    getCoverageEffectivenessService(ti)(tf)(limitTime)(username)
                  case "qeffectiveness" =>
                    getQueryEffectivenessService(ti)(tf)(limitTime)(username)
                  case "activebm" =>
                    getActiveOrRelevantBmService(ti)(tf)(None)(username)
                  case "score"    => getSearchScoreService(ti)(tf)(username)
                  case "pagestay" => getTotalPageStayService(ti)(tf)(username)
                  case "writingtime" => getWritingTimeService(ti)(tf)(username)
                  case "modquery" => getTotalModQueryService(ti)(tf)(username)
                  case "entropy" =>
                    getAverageQueryEntropyService(ti)(tf)(username)
                }

              })
              results
            }

  //Service to get participant init stage time
  val getStageTimeService = (username: String) => {

    val initTime = getInitTime(username)
    if (initTime.isEmpty) (username, 0.0.toDouble)
    else (username, initTime(0).localTimestamp)
  }

  //Service to get TotalCover for one participant
  val getTotalCoverService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal =
          and(
            filters,
            equal("username", username),
            equal("state", "PageEnter"),
            regex("url", "/page")
          )
        val documents = getVisitedDocumentsByPageEnter(filtersTotal)
        val totalCover: Option[Double] = Option(getTotalCover(documents))
        val end = System.currentTimeMillis
        (username, totalCover.getOrElse(0.0), init, end)

      }

  //Service to get BmRelevant to one participant
  val getActiveOrRelevantBmService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (relevant: Option[Boolean]) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val filters: Bson = defineFilters(username)(ti)(tf)
          val filtersTotal = and(
            filters,
            equal("username", username),
            equal("userMade", true),
            makeEqual[Boolean]("relevant", relevant)
          )
          val bookmarks =
            getBookmarksAndUnbookmarksRelevantsByUsername(filtersTotal)
          val bmRelevant: Option[Double] = Option(getBmRelevant(bookmarks))
          val end = System.currentTimeMillis

          (username, bmRelevant.getOrElse(0.0), init, end)
        }

  //Service to get UsfCover to one participant
  val getUsfCoverService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (limitTime: Int) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val filters: Bson = defineFilters(username)(ti)(tf)
          val filtersTotal =
            and(
              filters,
              equal("username", username),
              regex("url", "/page")
            )
          val documents = getVisitedDocumentsByPageEnter(filtersTotal)
          val usfCover: Option[Double] = Option(
            getUsfCover(documents, limitTime)
          )
          val end = System.currentTimeMillis
          (username, usfCover.getOrElse(0.0), init, end)
        }

  //Service to get Mouseclicks to one participant
  val getMouseClicksService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal =
          and(
            filters,
            equal("username", username),
            regex("url", "/page")
          )
        val mouseClicks: Option[Double] = Option(
          getMouseClicks(filtersTotal).toDouble
        )
        val end = System.currentTimeMillis
        (username, mouseClicks.getOrElse(0.0), init, end)
      }

  //Service to get MouseeCoordinates to one participant
  val getMouseCoordinatesService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal =
          and(
            filters,
            equal("username", username),
            regex("url", "/page")
          )
        val mouseCoordinates: Option[Double] = Option(
          getMouseCoordinates(filtersTotal).toDouble
        )
        val end = System.currentTimeMillis
        (username, mouseCoordinates.getOrElse(0.0), init, end)
      }

  //Service to get ScrollMoves to one participant
  val getScrollMovesService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal =
          and(
            filters,
            equal("username", username),
            regex("url", "/page")
          )
        val scrollMoves: Option[Double] = Option(
          getScrollMoves(filtersTotal).toDouble
        )
        val end = System.currentTimeMillis
        (username, scrollMoves.getOrElse(0.0), init, end)
      }

  //Service to get NumQueries to one participant
  val getNumQueryService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal = and(filters, equal("username", username))

        val queries: Seq[Queries] = getQueriesByUser(filtersTotal)
        val unrepeatedQueries =
          filterRepeated[Queries](queries, predicateQuery)
        val end = System.currentTimeMillis
        (username, unrepeatedQueries.length.toDouble, init, end)

      }

  //Service to get TotalModQuery to one participant
  val getTotalModQueryService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal = and(
          filters,
          equal("username", username),
          regex("url", "/search")
        )
        val filtersTotalQuery = and(filters, equal("username", username))

        val searchIntervals = getVisitedDocumentsByPageEnter(filtersTotal)
        val queries = getQueriesByUser(filtersTotalQuery)
        val keyStrokesByUser = getKeystrokesByuser(filtersTotal)

        val totalModQuery =
          getTotalModQuery(queries, searchIntervals, keyStrokesByUser, username)
        val end = System.currentTimeMillis
        (username, totalModQuery, init, end)
      }

  //Service to get WritingTime to one participant
  val getWritingTimeService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis

        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal = and(
          filters,
          equal("username", username),
          regex("url", "/search")
        )
        val filtersTotalQuery = and(filters, equal("username", username))

        val searchIntervals = getVisitedDocumentsByPageEnter(filtersTotal)
        val queries = getQueriesByUser(filtersTotalQuery)
        val keyStrokesByUser = getKeystrokesByuser(filtersTotal)

        val writingTime =
          getWritingTime(queries, searchIntervals, keyStrokesByUser, username)

        val end = System.currentTimeMillis

        (username, writingTime, init, end)
      }

  //Service to get AverageQueryEntropy to one participant
  val getAverageQueryEntropyService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal = and(
          filters,
          equal("username", username)
        )
        val queries = getQueriesByUser(filtersTotal)

        val averageQueryEntropy: Option[Double] = Option(
          getAverageQueryEntropy(queries)
        )
        val end = System.currentTimeMillis
        (username, averageQueryEntropy.getOrElse(0.0), init, end)
      }

  //Service to get TotalPageStay to one participant
  val getTotalPageStayService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val filters: Bson = defineFilters(username)(ti)(tf)
        val filtersTotal =
          and(
            filters,
            equal("username", username),
            regex("url", "/page")
          )
        val documents = getVisitedDocumentsByPageEnter(filtersTotal)
        val totalPageStay: Option[Double] = Option(getTotalPageStay(documents))
        val end = System.currentTimeMillis

        (username, totalPageStay.getOrElse(0.0), init, end)
      }

  //Service to get Precision to one participant
  val getPrecisionService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val totalCover: Tuple4[String, Double, Long, Long] =
          getTotalCoverService(ti)(tf)(username)
        val bmRelevant: Tuple4[String, Double, Long, Long] =
          getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)
        val precision = bmRelevant._2 / totalCover._2

        val end = System.currentTimeMillis
        (username, precision, init, end)
      }

  //Service to get Recall to one participant
  val getRecallService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (relevantDocuments: Double) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val bmRelevant: Tuple4[String, Double, Long, Long] =
            getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)
          val recall = bmRelevant._2 / relevantDocuments
          val end = System.currentTimeMillis
          (username, recall, init, end)
        }

  //Service to get F1/FScore to one participant
  val getFScoreService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (relevantDocuments: Double) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val totalCover: Tuple4[String, Double, Long, Long] =
            getTotalCoverService(ti)(tf)(username)
          val bmRelevant: Tuple4[String, Double, Long, Long] =
            getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)

          val precision: Double = bmRelevant._2 / totalCover._2
          val recall: Double = bmRelevant._2 / relevantDocuments
          val f1 = 2 * precision * recall / (precision + recall)
          val end = System.currentTimeMillis
          (username, f1, init, end)
        }

  //Service to get CoverageEffectiveness to one participant
  val getCoverageEffectivenessService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (limitTime: Int) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val totalCover: Tuple4[String, Double, Long, Long] =
            getTotalCoverService(ti)(tf)(username)
          val usfCover: Tuple4[String, Double,Long,Long] =
            getUsfCoverService(ti)(tf)(limitTime)(username)

          val coverageEffectiveness = usfCover._2 / totalCover._2
          val end = System.currentTimeMillis
          (username, coverageEffectiveness, init, end)
        }

  //Service to get QueryEffectiveness to one participant
  val getQueryEffectivenessService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (limitTime: Int) =>
        (username: String) => {
          val init = System.currentTimeMillis
          val coverageEffectiveness =
            getCoverageEffectivenessService(ti)(tf)(limitTime)(username)
          val numQueries = getNumQueryService(ti)(tf)(username)
          val queryEffectiveness = coverageEffectiveness._2 / numQueries._2
          val end = System.currentTimeMillis
          (username, queryEffectiveness, init, end)
        }

  //Service to get SearchScore to one participant
  val getSearchScoreService = (ti: Option[Int]) =>
    (tf: Option[Int]) =>
      (username: String) => {
        val init = System.currentTimeMillis
        val bmRelevant =
          getActiveOrRelevantBmService(ti)(tf)(Option(true))(username)
        val activeBm = getActiveOrRelevantBmService(ti)(tf)(None)(username)

        val searchScore: Option[Double] = Option(
          (bmRelevant._2 / activeBm._2) * 5
        )
        val end = System.currentTimeMillis
        (username, searchScore.getOrElse(0.0.toDouble), init, end)
      }

  //Function to create mongo query filters over range time
  val defineFilters = (username: String) =>
    (ti: Option[Int]) =>
      (tf: Option[Int]) => {

        val rangeTime = defineRangeTime(username, ti, tf)
        val filters: Bson = and(
          makeGte[Double]("localTimestamp", Option(rangeTime._1)),
          makeLte[Double]("localTimestamp", Option(rangeTime._2))
        )

        filters
      }
}
