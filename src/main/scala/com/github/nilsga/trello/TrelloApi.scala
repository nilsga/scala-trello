package com.github.nilsga.trello

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, ResponseEntity, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
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

  def member(memberId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Member] = {
    request[Member](s"/1/members/$memberId", params)
  }

  def board(boardId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Board] = {
    request[Board](s"/1/boards/$boardId", params)
  }

  def cards(boardId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Seq[Card]] = {
    request[Seq[Card]](s"/1/boards/$boardId/cards", params)
  }

  def card(cardId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Card] = {
    request[Card](s"/1/cards/$cardId", params)
  }

  def lists(boardId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext): Future[Seq[BoardList]] = {
    request[Seq[BoardList]](s"/1/boards/$boardId/lists", params)
  }

  def cardsForList(listId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Seq[Card]] = {
    request[Seq[Card]](s"/1/lists/$listId/cards", params)
  }

  def checklists(cardId: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext) : Future[Seq[Checklist]] = {
    request[Seq[Checklist]](s"/1/cards/$cardId/checklists", params)
  }

  private def request[T](path: String, params: Map[String, String] = Map())(implicit ec: ExecutionContext, unmarshaller: Unmarshaller[ResponseEntity, T]) : Future[T] = {
    Http().singleRequest(HttpRequest(uri = uri(path, params))).flatMap(resp => {
      resp.status match {
        case OK => Unmarshal(resp.entity).to[T]
        case _ =>
          val status = resp.status
          resp.entity.dataBytes.runWith(Sink.ignore)
          Future.failed(new RuntimeException(s"Request failed with status ${status.reason}"))
      }
    })
  }

  private def uri(path: String, params: Map[String, String]) = defaultUri
    .withPath(Uri.Path(path))
    .withQuery(Query(defaultParams ++ params))

}
