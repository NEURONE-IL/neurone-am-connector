package com.neurone.neuroneconector.metrics

import com.neurone.neuroneconector.db.collections._
import com.neurone.neuroneconector.functions.predicates._
import com.neurone.neuroneconector.functions.filters._
import com.neurone.neuroneconector.functions.reductions._
import com.neurone.neuroneconector.functions.mongoQueries._

/**Function declaration to calculate behavioral metrics**/
//Singleton objetc with functions to calculate behavioral metrics
package object simple {

  //Function to calculate TotalCover metric
  def getTotalCover(documents: Seq[Visitedlinks]) = {
    val unrepeatedDocuments =
      filterRepeated[Visitedlinks](documents, predicate).length
    unrepeatedDocuments
  }

  //Function to calculate BmRelevant or ActiveBm metric, depending on the given paremeters
  def getBmRelevant(bookmarks: Seq[Bookmarks]) = {
    val unrepeatedBookmarksAndUnbookmarksRelevantsByUsername =
      filterRepeatedSeq(bookmarks)
    val documentsNames = getDocumentsNames[Bookmarks](
      unrepeatedBookmarksAndUnbookmarksRelevantsByUsername,
      getNamesOfBookmarks
    )
    val unRepeatedDocumentsNames =
      filterRepeated[String](documentsNames, predicateNames)
    val captureBookmarksCount = countRelevantBookmarks(
      unrepeatedBookmarksAndUnbookmarksRelevantsByUsername,
      unRepeatedDocumentsNames
    )
    captureBookmarksCount
  }

  //Function to calculate UsfCover metric
  def getUsfCover(documents: Seq[Visitedlinks], limitTime: Int) = {
    val visitedDocumentsWithEnterAndExit = filterWithPageEnterAndExit(documents)
    val orderedVisitedDocuments = getPageStayByDocuementsGroup(
      visitedDocumentsWithEnterAndExit
    )
    val pageStayByDocument = filterByTime(orderedVisitedDocuments, limitTime)

    pageStayByDocument.length
  }

  //Function to calculate TotalPageStay metric
  def getTotalPageStay(documents: Seq[Visitedlinks]) = {
    val visitedDocumentsWithEnterAndExit = filterWithPageEnterAndExit(documents)
    val orderedVisitedDocuments = getPageStayByDocuementsGroup(
      visitedDocumentsWithEnterAndExit
    )
    val totalPageStay = sumElements(orderedVisitedDocuments)
    totalPageStay
  }

  //Function to calculate WritingTime metric
  def getWritingTime(
      queries: Seq[Queries],
      searchIntervals: Seq[Visitedlinks],
      keyStrokes: Seq[Keystrokes],
      username: String
  ) = {

    // println("Get Writting time for "+username)
    val searchIntervalsWithEnterAndExit = filterWithPageEnterAndExit(
      searchIntervals
    )
    // println("searchInterval",searchIntervalsWithEnterAndExit)
    val orderedSearchIntervals = getIntervals(searchIntervalsWithEnterAndExit)
    // println("ordered search intervals")
    val queriesAndIntervals =
      joinIntervalsAndQueries(queries, orderedSearchIntervals)
    // println("queries and intervals")
    val notEmptyIntervals = filterEmptyIntervals(queriesAndIntervals)
    // println("not empty intervals")
    val intervalWritingTime =
      getIntervalWritingTime(notEmptyIntervals, keyStrokes, username)
    // println("interval writting time")
    //   println("interval",intervalWritingTime)
    val totalWritingTime = sumElements(intervalWritingTime)

    totalWritingTime
  }

  //Function to calculate TotalModQuery metric
  def getTotalModQuery(
      queries: Seq[Queries],
      searchIntervals: Seq[Visitedlinks],
      keyStrokes: Seq[Keystrokes],
      username: String
  ) = {

    val searchIntervalsWithEnterAndExit = filterWithPageEnterAndExit(
      searchIntervals
    )
    val orderedSearchIntervals = getIntervals(searchIntervalsWithEnterAndExit)

    val queriesAndIntervals =
      joinIntervalsAndQueries(queries, orderedSearchIntervals)
    val notEmptyIntervals = filterEmptyIntervals(queriesAndIntervals)
    val modByInterval =
      getIntervalModQuery(notEmptyIntervals, keyStrokes, username)

    val totalModQuery = sumElements(modByInterval)
    totalModQuery
  }

  
  //Function to calculate AverageQueryEntropy metric
  def getAverageQueryEntropy(queries: Seq[Queries]) = {
    val unrepeatedQueries =
      filterRepeated[Queries](queries, predicateQuery)
    val queryEntropyByQuery =
      unrepeatedQueries.map(query => getQueryEntropy(query.query))
    val averageQueryEntropy = sumElements(queryEntropyByQuery) / unrepeatedQueries.length
    
    averageQueryEntropy
  }
}
