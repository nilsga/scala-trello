package com.github.nilsga.trello

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class TrelloApiIntegrationTest extends WordSpec with Matchers {

  val config = ConfigFactory.load()
  implicit val actorSystem = ActorSystem("TrelloApiIntegrationTests", config)
  import actorSystem.dispatcher

  val api = TrelloApi(config.getString("trello.key"), config.getString("trello.token"))

  "Trello API" should {

    "Get member" in {
      val member = Await.result(api.member("me"), 10 seconds)
      member.fullName should be("Trello Async Client")
    }

    "Get board" in {
      val board = Await.result(api.board("5618cf5e0c80b6fd6b667971"), 10 seconds)
      board.name should be("Trello Integration Tests")
    }

    "Get cards for board" in {
      val cards = Await.result(api.cards("5618cf5e0c80b6fd6b667971"), 10 seconds)
      cards.head.desc should be("Test card description")
    }

    "Get lists for board" in {
      val lists = Await.result(api.lists("5618cf5e0c80b6fd6b667971"), 10 seconds)
      lists.length should be(1)
      val list = lists.head
      list.name should be("Integration tests")
    }

    "Get card" in {
      val card = Await.result(api.card("5618cf67739728febefa2980"), 10 seconds)
      card.desc should be("Test card description")
      card.idMembers.length should be(1)
    }

    "Get checklists for card" in {
      val checklists = Await.result(api.checklists("5618cf67739728febefa2980"), 10 seconds)
      checklists.length should be(2)
      val checklist = checklists.head
      checklist.id should be("5618cf805e5861567fd14559")
    }
  }
}
