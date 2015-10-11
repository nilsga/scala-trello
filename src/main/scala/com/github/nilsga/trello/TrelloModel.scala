package com.github.nilsga.trello

object TrelloModel {
  case class Member(id: String, username: String, fullName: String, idBoards: Seq[String])
  case class Board(id: String, name: String, desc: String, url: String, shortUrl: String)
  case class Card(id: String, desc: String, idChecklists: Seq[String])
  case class Checklist(id: String, name: String, idBoard: String, idCard: String, checkItems: Seq[ChecklistItem])
  case class ChecklistItem(id: String, name: String, state: String)
}