package com.neurone.neuroneconector.functions

import com.neurone.neuroneconector.db.collections._
import com.neurone.neuroneconector.functions.filters._
import org.mongodb.scala.model.Filters._
import com.neurone.neuroneconector.functions.mongoQueries._
import org.mongodb.scala.bson.conversions.Bson

//Singleton object with a set of reducer functions
package object reductions {

  //Function to count relevant bookmarks quantity
  def countRelevantBookmarks(
      bookmarks: Seq[Bookmarks],
      documents: List[String]
  ) =
    documents.foldLeft(0: Int)(
      (acumulator, actualElement) =>
        acumulator + verifyDocumentCapture(
          filterByDoc(actualElement, bookmarks)
        )
    )

  //Function to count effective bookmarks
  def verifyDocumentCapture(bookmarks: Seq[Bookmarks]) =
    if (bookmarks.foldLeft(0: Int)((counter, actualElement) => {
          if (actualElement.action == "Bookmark") counter + 1 else counter - 1
        }) > 0) 1
    else 0

  //Function to apply other function to  Seq of type T
  def getDocumentsNames[T](elements: Seq[T], function: T => String) =
    elements.map(function)

  //Function to sum all elementos of Seq[Double]
  def sumElements(documentsPageStay: Seq[Double]): Double = {
    documentsPageStay.foldLeft(0.0.toDouble: Double)(
      (acumulator, actualElement) => acumulator + actualElement
    )
  }

  //Function to get Links intervals
  def getIntervals(
      documents: Seq[Visitedlinks]
  ): Seq[Tuple2[Double, Double]] = {

    documents.zipWithIndex.foldLeft(Seq(): Seq[Tuple2[Double, Double]])(
      (acumulator, actualElement) =>
        if (actualElement._2 == 0 || actualElement._2 % 2 == 0)
          acumulator :+ (actualElement._1.localTimestamp, documents(
            actualElement._2 + 1
          ).localTimestamp)
        else acumulator
    )
  }

  //Function to join queries in each interval
  def joinIntervalsAndQueries(
      queries: Seq[Queries],
      intervals: Seq[Tuple2[Double, Double]]
  ): Seq[Tuple3[Double, Double, Seq[Queries]]] = {

    intervals.map(interval => {
      val intervalQueries = queries.filter(
        query =>
          query.localTimestamp >= interval._1 && query.localTimestamp <= interval._2
      )
      (interval._1, interval._2, intervalQueries)
    })
  }

  //Function to get specific query WritingTime
  def getQueryWritingTime(
      queriesInterval: Seq[Queries],
      keyStrokesInterval: Seq[Keystrokes]
  ): Seq[Tuple2[Double, Double]] = {

    queriesInterval.zipWithIndex.map(query => {
      println(query)

      val finalTime =
        if (query._2 == 0) 0 else queriesInterval(query._2 - 1).localTimestamp
      val validKey: Option[Keystrokes] = keyStrokesInterval.find(
        key =>
          key.localTimestamp <= query._1.localTimestamp &&
            key.localTimestamp >= finalTime && key.keyCode != 13
      )
      val initTimeKey = validKey.getOrElse(Keystrokes("", 0, 0.0))
      val initTime = initTimeKey.localTimestamp
      val endTime = if (initTime == 0.0) 0.0 else query._1.localTimestamp
      (initTime, endTime)
    })
  }

  //Function to get WritingTime for a search interval
  def getIntervalWritingTime(
      intervals: Seq[Tuple3[Double, Double, Seq[Queries]]],
      keyStrokes: Seq[Keystrokes],
      username: String
  ): Seq[Double] = {

    intervals.map(interval => {
      val intervalKeystrokes = keyStrokes.filter(
        key =>
          key.localTimestamp >= interval._1 &&
            key.localTimestamp <= interval._2
      )
      val queriesWritingInterval =
        getQueryWritingTime(interval._3, intervalKeystrokes)
      val intervalWritingTime =
        sumElements(
          queriesWritingInterval.map(time => (time._2 - time._1) / 1000)
        )
      intervalWritingTime
    })
  }

  //Function to get Query modification times
  def getQueryMod(
      queriesInterval: Seq[Queries],
      keyStrokesInterval: Seq[Keystrokes]
  ): Seq[Double] = {

    queriesInterval.zipWithIndex.map(
      query => {

        val finalTime =
          if (query._2 == 0) 0 else queriesInterval(query._2 - 1).localTimestamp
        val validKey: Option[Keystrokes] = keyStrokesInterval.find(
          key =>
            key.localTimestamp <= query._1.localTimestamp &&
              key.localTimestamp >= finalTime && key.keyCode != 13
        )
        val initTimeKey = validKey.getOrElse(Keystrokes("", 0, 0.0))
        val initTime = initTimeKey.localTimestamp
        val endTime = if (initTime == 0.0) 0.0 else query._1.localTimestamp
        val queryIntervalKeyStrokes = keyStrokesInterval.filter(
          key => key.localTimestamp >= initTime && key.localTimestamp <= endTime
        )
        val modQuery =
          queryIntervalKeyStrokes.zipWithIndex
            .foldLeft(0)(
              (acumulator, actualElement) =>
                if (actualElement._2 != 0 && queryIntervalKeyStrokes(
                      actualElement._2 - 1
                    ).keyCode != 8 &&
                    queryIntervalKeyStrokes(actualElement._2 - 1).keyCode != 13 &&
                    queryIntervalKeyStrokes(actualElement._2 - 1).keyCode != 46 &&
                    (actualElement._1.keyCode == 8 || actualElement._1.keyCode == 46))
                  acumulator + 1
                else acumulator
            )
        if (modQuery > 0) println(modQuery)
        modQuery.toDouble
      }
    )
  }


  //Function to get ModQuery for a search interval
  def getIntervalModQuery(
      intervals: Seq[Tuple3[Double, Double, Seq[Queries]]],
      keyStrokes: Seq[Keystrokes],
      username: String
  ): Seq[Double] = {

    intervals.map(interval => {
      val intervalKeystrokes = keyStrokes.filter(
        key =>
          key.localTimestamp >= interval._1 &&
            key.localTimestamp <= interval._2
      )
      val queriesMod =
        getQueryMod(interval._3, intervalKeystrokes)
      val intervalModQuery =
        sumElements(
          queriesMod
        )
      intervalModQuery
    })
  }

  //Function to get query entropy for one query
  def getQueryEntropy(query: String): Double = {

    val states: Seq[String] = query.split(" ").toSeq
    val unrepeatedStates = states.toSet
    val total = states.length

    val statesProbabilities = unrepeatedStates.toSeq.map(
      state => states.count(element => element == state).toDouble / total
    )
 
    val log2 = (num: Double) => Math.log10(num) / Math.log10(2.0)
    val queryEntropy = statesProbabilities.foldLeft(0.0.toDouble: Double)(
      (acumulator, p) => acumulator - p * log2(p)
    )
    println(queryEntropy)
    queryEntropy
  }

  //Function to get PageStay for each links pair
  val getPageStayByDocuementsGroup
      : Seq[Visitedlinks] => Seq[Double] = (documents: Seq[Visitedlinks]) =>
    documents.zipWithIndex.foldLeft(Seq(0.0.toDouble): Seq[Double])(
      (acumulator, actualElement) =>
        if (actualElement._2 == 0 || actualElement._2 % 2 == 0)
          acumulator :+ ((documents(actualElement._2 + 1).localTimestamp - actualElement._1.localTimestamp) / 1000)
        else acumulator
    )

  //Function to get range time for a given ti-tf parameter
  def defineRangeTime(
      username: String,
      ti: Option[Int],
      tf: Option[Int]
  ): Tuple2[Double, Double] = {

    val valueTi = ti.getOrElse(0)
    val valueTf = tf.getOrElse(0)
    val end: Double = System.currentTimeMillis.toDouble
    //val end: Double = (1485859583391.0).toDouble
    (valueTi, valueTf) match {
      case (0, 0) => (valueTi.toDouble, valueTf.toDouble)
      case (0, _) => (end - valueTf * 1000, end)
      case (_, 0) => {
        val initTime: Seq[Visitedlinks] = getInitTime(username)
        if (initTime.isEmpty) ((0.0).toDouble, (0.0).toDouble)
        else {
          (
            initTime(0).localTimestamp,
            initTime(0).localTimestamp + valueTi * 1000
          )
        }
      }
      case (_) => {
        val initTime: Seq[Visitedlinks] = getInitTime(username)
        if (initTime.isEmpty) ((0.0).toDouble, (0.0).toDouble)
        else {
          (initTime(0).localTimestamp + valueTi * 1000, end - valueTf * 1000)
        }
      }
    }
  }
  // def extract(p: Person, fieldName: String) = {
  //   fieldName match {
  //     case "name" => Some(p.name)
  //     case "age"  => Some(p.age)
  //     case _      => None
  //   }
  // }
}
