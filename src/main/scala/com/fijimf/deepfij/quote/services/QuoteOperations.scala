package com.fijimf.deepfij.quote.services

import com.fijimf.deepfij.quote.model.Quote

trait QuoteOperations[F[_]]{
  def healthcheck: F[Boolean]

  def insertQuote(a: Quote): F[Quote]

  def updateQuote(a: Quote): F[Quote]

  def deleteQuote(id: Long): F[Int]

  def listQuotes(): F[List[Quote]]

  def findQuote(id: Long): F[Option[Quote]]

  def randomQuote(): F[Quote]

  def randomQuote(tag:String, pct:Double): F[Quote]
}
