package com.neurone.neuroneconector.functions

import com.neurone.neuroneconector.db.collections._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.bson.conversions.Bson

//Set of predicates used in differents filters and functions
package object predicates {

  //Predicate to compare two visitedlinks url
  val predicate = (actualElement: Visitedlinks) =>
    (element: Visitedlinks) => actualElement.url == element.url
  //Predicate to compare two strings
  val predicateNames = (actualDoc: String) => (doc: String) => actualDoc == doc
  //Function to get bookmarks url
  val getNamesOfBookmarks = (element: Bookmarks) => element.url
  //Preficate to compare two queries
  val predicateQuery= (actualQuery: Queries)=>(query: Queries)=> actualQuery.query.toLowerCase()==query.query.toLowerCase()

}
