package com.github.nilsga.trello

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.{HttpRequest, ResponseEntity, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.github.nilsga.trello.TrelloModel._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.{DefaultFormats, Formats, Serialization, jackson}

import scala.concurrent.{ExecutionContext, Future}

object TrelloApi  {

  def apply(key: String, token: String)(implicit actorSystem: ActorSystem = ActorSystem("TrelloApi")) = new TrelloApi(key, token)(actorSystem, ActorMaterializer())
}

class TrelloApi(val key: String, val token: String)(implicit val actorSystem: ActorSystem, materializer: ActorMaterializer) {

  val apiEndpoint = "https://api.trello.com"
  val defaultUri = Uri(apiEndpoint)
  val defaultParams = Map("key" -> key, "token" -> token)
  implicit val formats: Formats = DefaultFormats
  implicit val jacksonSerialization: Serialization = jackson.Serialization

  def member(id: String)(implicit ec: ExecutionContext) : Future[Member] = {
    request[Member](s"/1/members/$id")
  }

  def board(id: String)(implicit ec: ExecutionContext) : Future[Board] = {
    request[Board](s"/1/boards/$id")
  }

  def cards(id: String)(implicit ec: ExecutionContext) : Future[Seq[Card]] = {
    request[Seq[Card]](s"/1/boards/$id/cards")
  }

  def card(id: String)(implicit ec: ExecutionContext) : Future[Card] = {
    request[Card](s"/1/cards/$id")
  }

  private def request[T](path: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext, unmarshaller: Unmarshaller[ResponseEntity, T]) : Future[T] = {
    Http().singleRequest(HttpRequest(uri = uri(path, params))).flatMap(resp => {
      resp.status match {
        case OK => Unmarshal(resp.entity).to[T]
        case _ => Unmarshal(resp.entity).to[String].flatMap(entity => Future.failed(new RuntimeException(entity)))
      }
    })
  }

  private def uri(path: String, params: Map[String, String]) = defaultUri
    .withPath(Uri.Path(path))
    .withQuery(defaultParams ++ params)

}
