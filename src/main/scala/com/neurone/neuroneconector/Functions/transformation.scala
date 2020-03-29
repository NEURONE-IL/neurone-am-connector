package com.neurone.neuroneconector.functions
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.BsonDocument

//Functions to generate query filters for mongo queries
package object transformation {

  //Generate equal query
  def makeEqual[T](field: String, value: Option[T]): Bson = {

    val resultValue = value.getOrElse(0)
    resultValue match {
      case 0 => BsonDocument.apply()
      case _ => equal(field, resultValue)
    }
  }

  //Generate greater than and equal query
  def makeGte[T](field: String, value: Option[T]): Bson = {

    val resultValue = value.getOrElse(0)
    resultValue match {
      case 0 => BsonDocument.apply()
      case _ => gte(field, resultValue)
    }
  }

  //Generate tess than and equal query
  def makeLte[T](field: String, value: Option[T]): Bson = {

    val resultValue = value.getOrElse(0)
    resultValue match {
      case 0 => BsonDocument.apply()
      case _ => lte(field, resultValue)
    }
  }

  //Join gte and lte queries
  def makeRange[T](field: String, value: Tuple2[T, T]): Bson = {

    value match {
      case (0, 0) => BsonDocument.apply()
      case _      => and(gte(field, value._1), lte(field, value._2))
    }
  }

}
