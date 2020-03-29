package com.neurone.neuroneconector.functions

import com.neurone.neuroneconector.db.collections._
import com.neurone.neuroneconector.functions.reductions._
import com.neurone.neuroneconector.functions.predicates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.conversions.Bson

/**Filters declaration**/
//Singleton object with filers function
package object filters {

  //Filter a List of bookmarks object by document url
  def filterByDoc(element: String, bookmarks: Seq[Bookmarks]) =
    bookmarks.filter(action => action.url == element)

  //Filter to list of Doubles greater than limit
  def filterByTime(
      documentsPageStay: Seq[Double],
      limitTime: Int
  ): Seq[Double] = {
    documentsPageStay.filter(pageStay => pageStay >= limitTime)
  }

  //Parametrized function to filter repeated elements of type T
  def filterRepeated[T](documents: Seq[T], predicate: T => T => Boolean) =
    documents.foldLeft(List(): List[T])((acumulator, actualElement) => {
      if (acumulator.exists(predicate(actualElement))) acumulator
      else actualElement :: acumulator
    })

  //Function to delete repeated links
  def filterWithPageEnterAndExit(
      documents: Seq[Visitedlinks]
  ): Seq[Visitedlinks] = {

    val firstFilter = filterRepeatedPageEnter(documents)
    val secondFilter = filterSingleLinks(firstFilter)
    val thirdFilter = filterRepeatedPageExit(secondFilter)

    thirdFilter
  }

  //Function to delete repeated links with PageEnter state without a PageExit
  def filterRepeatedPageEnter(
      documents: Seq[Visitedlinks]
  ): Seq[Visitedlinks] = {

    documents.zipWithIndex
      .filter(
        pair =>
          pair._2 == 0 || pair._1.state == "PageExit" ||
            pair._1.state != documents(pair._2 - 1).state || pair._1.url != documents(
            pair._2 - 1
          ).url
      )
      .map(document => document._1)
  }

  //Function to delete links without other pair
  def filterSingleLinks(documents: Seq[Visitedlinks]): Seq[Visitedlinks] = {
    documents.zipWithIndex
      .filter(
        pair =>
          pair._1.state == "PageExit" ||
            (pair._2 != documents.length - 1 && pair._1.state != documents(
              pair._2 + 1
            ).state)
      )
      .map(document => document._1)
  }


  //Function to deleate repeated links with PageExit state
  def filterRepeatedPageExit(
      documents: Seq[Visitedlinks]
  ): Seq[Visitedlinks] = {

    documents.zipWithIndex
      .filter(
        pair =>
          pair._2 == documents.length - 1 ||
            pair._1.state != documents(pair._2 + 1).state
      )
      .map(document => document._1)
  }

  //Function to delete repeated in a ist of Bookmarks
  def filterRepeatedSeq(elements: Seq[Bookmarks]) =
    elements.zipWithIndex
      .filter(
        pair =>
          pair._2 == 0 || elements(pair._2 - 1).url != pair._1.url
            || elements(pair._2 - 1).action != pair._1.action
      )
      .map(element => element._1)

  //Function to filter repeated queries in a lis of queries objects
  def filterRepeatedQuery(queries: Seq[Queries]) = {

    queries.zipWithIndex
      .filter(
        pair => pair._2 == 0 || pair._1.query != queries(pair._2 - 1).query
      )
      .map(element => element._1)
  }

  //Function to filteer empty queries intervals
  def filterEmptyIntervals(
      intervals: Seq[Tuple3[Double, Double, Seq[Queries]]]
  ): Seq[Tuple3[Double, Double, Seq[Queries]]] = {
    intervals.filter(interval => interval._3.length > 0)
  }
}
