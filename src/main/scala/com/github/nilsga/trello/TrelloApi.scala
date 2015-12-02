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

  def member(memberId: String)(implicit ec: ExecutionContext) : Future[Member] = {
    request[Member](s"/1/members/$memberId")
  }

  def board(boardId: String)(implicit ec: ExecutionContext) : Future[Board] = {
    request[Board](s"/1/boards/$boardId")
  }

  def cards(boardId: String)(implicit ec: ExecutionContext) : Future[Seq[Card]] = {
    request[Seq[Card]](s"/1/boards/$boardId/cards")
  }

  def card(cardId: String)(implicit ec: ExecutionContext) : Future[Card] = {
    request[Card](s"/1/cards/$cardId")
  }

  def lists(boardId: String)(implicit ec: ExecutionContext): Future[Seq[BoardList]] = {
    request[Seq[BoardList]](s"/1/boards/$boardId/lists")
  }

  def cardsForList(listId: String)(implicit ec: ExecutionContext) : Future[Seq[Card]] = {
    request[Seq[Card]](s"/1/lists/$listId/cards")
  }

  def checklists(cardId: String)(implicit ec: ExecutionContext) : Future[Seq[Checklist]] = {
    request[Seq[Checklist]](s"/1/cards/$cardId/checklists")
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
