package com.neurone.neuroneconector.concurrency.actors

import akka.actor._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent._
import java.util.concurrent.Executors
import scala.concurrent.duration._
import java.net.URLEncoder
import com.neurone.neuroneconector.functions.mongoQueries.getUsernames
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.conversions.Bson

/**AKKA actor function declaration**/
//Object that rperesent a worker execution instance
object Worker {
  def props(
      username: String,
      f: (String) => Tuple4[String, Double,Long,Long]
  ): Props =
    Props(new Worker(username, f))
  case object Execute
}

//Class definition of worker
class Worker(username: String, f: (String) => Tuple4[String, Double,Long,Long])
    extends Actor {
  import Worker._

  def receive = {
    case Execute =>
      sender ! f(username)
    case _ =>
      println("Me caigo")

  }
}

//Singleton object with actor functions
package object actorFunctions {

  import Worker._

  implicit val ec =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  //Function to run a Actor instance
  val runActors =
    (system: ActorSystem) =>
      (function: (String) => Tuple4[String, Double,Long,Long]) =>
        (username: String) => {

          implicit val timeout = Timeout(Duration(100, TimeUnit.SECONDS))
          val name = URLEncoder.encode(username)
          val myActor =
            system.actorOf(
              Worker.props(username, function),
              name = name
            )
          ask(myActor, Execute).mapTo[Tuple4[String, Double,Long,Long]]
        }

  //Function to call actor execution for all participants
  val parrallelizeWithActors = (function: (String) => Tuple4[String, Double,Long,Long]) =>
    (system: ActorSystem) =>
      (usersnames: Seq[String]) => {
        val results =
          Future.traverse(usersnames)(runActors(system)(function))
        results
      }
}
