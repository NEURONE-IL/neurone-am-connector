package com.neurone.neuroneconector.services


import com.neurone.neuroneconector.functions.mongoQueries._
import com.neurone.neuroneconector.services.service._
import scala.concurrent.Future
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import java.net.URLEncoder
import scala.concurrent.duration.Duration
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.neurone.neuroneconector.concurrency.actors.actorFunctions._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.conversions.Bson

/**Services for all declaration**/
//Singleton object to get metrics values for all participants
package object serviceAll {

  // Function to parallelize a metric calculation for all participants with actor model
  val makeActors = (function: (String) => Tuple2[String, Double]) => {

    val usersnames = getUsernames()
    val system = ActorSystem("neuroneSystem")
    val resultsFuture =
      parrallelizeWithActors(function)(system)(usersnames)
    val results = Await.result(resultsFuture, Duration(10, TimeUnit.SECONDS))
    results
  }

  //Function to calculate metric value for all participal in secuential mode
  val makeMonolith = (function: (String) => Tuple2[String, Double]) => {
    val usersnames = getUsernames()
    usersnames.map(u => function(u))
  }

  //Service to get TotalCover for all participants
  def getTotalCoverServiceForAll(
      ti: Option[Int],
      tf: Option[Int]
  ): Seq[Tuple2[String, Double]] = {
    makeActors(getTotalCoverService(ti)(tf))
    //makeMonolith(getTotalCoverService(ti)(tf))
  }

    //Service to get ActiveBm for all participants
  def getRelevantOrActiveBmServiceForAll(
      ti: Option[Int],
      tf: Option[Int],
      relevant: Option[Boolean]
  ): Seq[Tuple2[String, Double]] = {
    makeActors(getActiveOrRelevantBmService(ti)(tf)(relevant))
    //makeMonolith(getActiveOrRelevantBmService(ti)(tf)(relevant))
  }

    //Service to get UsfCover for all participants
  def getUsfCoverServiceForAll(
      limitTime: Int,
      ti: Option[Int],
      tf: Option[Int]
  ): Seq[Tuple2[String, Double]] = {

    makeActors(getUsfCoverService(ti)(tf)(limitTime))
    //makeMonolith(getUsfCoverService(ti)(tf)(limitTime))
  }

  //Service to get TotalPageStay for all participants
  def getTotalPageStayServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getTotalPageStayService(ti)(tf))
    //makeMonolith(getTotalPageStayService(ti)(tf))
  }

  //Service to get NumQueries for all participants
  def getNumQueriesServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getNumQueryService(ti)(tf))
    //makeMonolith(getNumQueryService(ti)(tf))
  }

    //Service to get MouseClicksService for all participants
  def getMouseclicksServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getMouseClicksService(ti)(tf))
    //makeMonolith(getMouseClicksService(ti)(tf))
  }

    //Service to get MouseCoordinates for all participants
  def getMouseCoordinatesServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getMouseCoordinatesService(ti)(tf))
    //makeMonolith(getMouseClicksService(ti)(tf))
  }

    //Service to get ScrollMoves for all participants
  def getScrollMovesServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getScrollMovesService(ti)(tf))
    //makeMonolith(getScrollMovesService(ti)(tf))
  }

  //Service to get WritingTime for all participants
  def getWritingTimeServiceForAll(ti: Option[Int], tf: Option[Int]) = {
    makeActors(getWritingTimeService(ti)(tf))
    //makeMonolith(getWritingTimeService(ti)(tf))
  }

  //Service to get TotalModQuery for all participants
  def getTotalModQueryServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getTotalModQueryService(ti)(tf))
    //makeMonolith(getTotalModQueryService(ti)(tf))
  }

  //Service to get AverageQueryEntropy for all participants
  def getAverageQueryEntropyServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getAverageQueryEntropyService(ti)(tf))
    //makeMonolith(getAverageQueryEntropyService(ti)(tf))
  }

  //Service to get Precision for all participants
  def getPrecisionServiceForAll(ti: Option[Int], tf: Option[Int]) = {
    makeActors(getPrecisionService(ti)(tf))
    //makeMonolith(getPrecisionService(ti)(tf))
  }
  
  //Service to get Recall for all participants
  def getRecallServiceForAll(
      relevants: Double,
      ti: Option[Int],
      tf: Option[Int]
  ) = {
    makeActors(getRecallService(ti)(tf)(relevants))
    //makeMonolith(getRecallService(ti)(tf)(relevants))
  }

  //Service to get F1/Fscore for all participants
  def getF1ServiceForAll(
      relevants: Double,
      ti: Option[Int],
      tf: Option[Int]
  ) = {
    makeActors(getFScoreService(ti)(tf)(relevants))
    //makeMonolith(getFScoreService(ti)(tf)(relevants))
  }

  //Service to get SearchScore for all participants
  def getSearchScoreServiceForAll(ti: Option[Int], tf: Option[Int]) = {

    makeActors(getSearchScoreService(ti)(tf))
    //makeMonolith(getSearchScoreService(ti)(tf))
  }

  //Service to get CoverageEffectiveness for all participants  
  def getCoverageEffectivenessServiceForAll(
      ti: Option[Int],
      tf: Option[Int],
      limitTime: Int
  ) = {

    makeActors(getCoverageEffectivenessService(ti)(tf)(limitTime))
    //makeMonolith(getCoverageEffectivenessService(ti)(tf)(limitTime))
  }

  //Service to get QueryEffectiveness for all participants  
  def getQueryEffectivenessServiceForAll(
      ti: Option[Int],
      tf: Option[Int],
      limitTime: Int
  ) = {

    makeActors(getQueryEffectivenessService(ti)(tf)(limitTime))
    //makeMonolith(getQueryEffectivenessService(ti)(tf)(limitTime))
  }

}



// def getPrecisionServiceForAll()={
//     val rdd=getRdd()
//     val precisionAll= (username: String) =>getPrecisionService(username)
//     val results=rdd.map(precisionAll)
//     results.collect()
// }

// def getPrecisionServiceForAllFuture() ={
//     val results=parallelizeFunction(getPrecisionService)
//     val r=Await.result(results, Duration(10, TimeUnit.SECONDS))
//     r
// }
