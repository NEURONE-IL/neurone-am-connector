package com.neurone.neuroneconector.functions

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import com.neurone.neuroneconector.db.conection._
import com.neurone.neuroneconector.db.tour.Helpers._
import com.neurone.neuroneconector.db.collections._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.BsonDocument

//Singleton object with differents mongo queries
package object mongoQueries {

  //Get Visited documents by a provide filter
  def getVisitedDocumentsByPageEnter(filters: Bson) = {
    DB.visitedlinks
      .find(filters)
      .results()
  }

  //Get Bookmarks by a given filter query
  def getBookmarksAndUnbookmarksRelevantsByUsername(
      filters: Bson
  ) =
    DB.bookmarks
      .find(filters)
      .sort(ascending("localTimestamp"))
      .results()

  //Get all participants of the session
  def getUsernames() = {
    DB.userdata.find().results().map(user => user.username)
  }

  //Get init stage time for one participant
  def getInitTime(username: String) =
    DB.visitedlinks
      .find(
        and(
          equal("username", username),
          equal("url", "/tutorial?stage=search")
        )
      )
      .results()

  //Get all KeyStrokes by a given filter
  def getKeystrokesByuser(filters: Bson) = DB.keystrokes.find(filters).results()

  //Get all Queries by a given filter
  def getQueriesByUser(filters: Bson) = DB.queries.find(filters).results()

  //Get all MouseClicks by a given filter query
  def getMouseClicks(filters: Bson): Double =
    DB.mouseClicks.countDocuments(filters).headResult()

  //Get all MouseCoordinates by a given filter query
  def getMouseCoordinates(filters: Bson): Double =
    DB.mouseCoordinates.countDocuments(filters).headResult()

  //Get all ScrollMoves by a given filter query  
  def getScrollMoves(filters: Bson): Double =
    DB.mouseCoordinates.countDocuments(filters).headResult()
}
