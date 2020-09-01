package com.neurone.neuroneconector

import cats.effect.IO
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._
import com.neurone.neuroneconector.endpoints.endpoints._
import com.typesafe.config._

// Main Function to init the program
object Main extends App {

  //App port
  val config = ConfigFactory.load()
  val port = config.getString("app.PORT")
  val policy: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST")),
    allowsHeaders = _ => Some(Seq("Accept"))
  )
  println("PORT", port)

  //Load all endpoints
  def service: Service[Request, Response] =
    Bootstrap
      .serve[Application.Json](combined)
      .toService

  val corsService: Service[Request, Response] =
    new Cors.HttpFilter(policy).andThen(service)
  // Init services on port 8081
  Await.ready(Http.server.serve(":" + port, corsService))
}
