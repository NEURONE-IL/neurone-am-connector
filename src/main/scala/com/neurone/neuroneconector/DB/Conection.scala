package com.neurone.neuroneconector.db.conection

import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries._
import com.neurone.neuroneconector.db.collections._
import com.typesafe.config._

//Singleton object to generate db conecction
object DB {

  val codecRegistry = fromRegistries(
    fromProviders(
      classOf[Visitedlinks],
      classOf[Bookmarks],
      classOf[UserData],
      classOf[Queries],
      classOf[Keystrokes]
    ),
    DEFAULT_CODEC_REGISTRY
  )


  val config = ConfigFactory.load()
  val mongoUrl=config.getString("mongo.MONGO_URL")
  val mongoDB=config.getString("mongo.MONGO_DB")
  println("Url %s", mongoUrl)
  println("DB %s", mongoDB)

  //Init connection
  val mongoClient: MongoClient = MongoClient(mongoUrl)
  val database: MongoDatabase =
    mongoClient.getDatabase(mongoDB).withCodecRegistry(codecRegistry)

  //Collections declaration
  val visitedlinks: MongoCollection[Visitedlinks] =
    database.getCollection("visitedlinks")
  val bookmarks: MongoCollection[Bookmarks] =
    database.getCollection("bookmarks")
  val userdata: MongoCollection[UserData] = database.getCollection("userdata")

  val keystrokes: MongoCollection[Keystrokes] =
    database.getCollection("keystrokes")

  val queries: MongoCollection[Queries] = database.getCollection("queries")
  val mouseClicks: MongoCollection[Document] =
    database.getCollection("mouseclicks")
  val mouseCoordinates: MongoCollection[Document] =
    database.getCollection("mousecoordinates")
  val scrollMoves: MongoCollection[Document] =
    database.getCollection("scrollmoves")
}
