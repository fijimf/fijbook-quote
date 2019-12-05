package com.fijimf.deepfij.quote.services

import cats.MonadError
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.quote.model.Quote
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.util.Random

class QuoteRepo[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {

  val me: MonadError[F, Throwable] = implicitly[MonadError[F, Throwable]]
  val nullQuote: Quote = Quote(-1L, "No quotes loaded", "", None, None)

  def healthcheck: F[Boolean] = {
    doobie.FC.isValid(2 /*timeout in seconds*/).transact(xa)
  }

  def insertQuote(a: Quote): F[Quote] = {
    import Quote.Dao._
    insert(a)
      .withUniqueGeneratedKeys[Quote](cols: _*)
      .transact(xa).exceptSql(ex => me.raiseError[Quote](ex))
  }

  def updateQuote(a: Quote): F[Quote] = {
    import Quote.Dao._
    update(a)
      .withUniqueGeneratedKeys[Quote](cols: _*)
      .transact(xa)
  }

  def deleteQuote(id: Long): F[Int] = {
    Quote.Dao.delete(id).run.transact(xa)
  }

  def listQuotes(): F[List[Quote]] = Quote.Dao.list().to[List].transact(xa)

  def findQuote(id: Long): F[Option[Quote]] = Quote.Dao.find(id).option.transact(xa)

  def randomQuote(): F[Quote] = {
    for {
      qs<-Quote.Dao.list().to[List].transact(xa)
      q<-F.delay(Random.shuffle(qs).headOption)
    } yield {
      q.getOrElse(nullQuote)
    }
  }

  def randomQuote(tag:String, pct:Double): F[Quote] = {
    for {
      quotes <- Quote.Dao.list().to[List].transact(xa)
      quotesWithTag = quotes.filter(_.tag.contains(tag))
      x <- F.delay(Random.nextDouble())
      q <- if (x < pct && quotesWithTag.nonEmpty) {
        F.delay(Random.shuffle(quotesWithTag).headOption)
      } else {
        F.delay(Random.shuffle(quotes).headOption)
      }
    } yield {
      q.getOrElse(nullQuote)
    }
  }

}