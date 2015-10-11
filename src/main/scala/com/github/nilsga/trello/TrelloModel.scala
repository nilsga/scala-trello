package com.github.nilsga.trello

object TrelloModel {
  case class Member(id: String, username: String, fullName: String, idBoards: Seq[String])
  case class Board(id: String, name: String, desc: String, url: String, shortUrl: String)
  case class Card(id: String, desc: String, idChecklists: Seq[String])
}