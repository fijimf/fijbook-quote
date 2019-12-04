package com.fijimf.deepfij.quote.services

import cats.MonadError
import cats.effect.Sync
import com.fijimf.deepfij.quote.model.Quote
import doobie.implicits._
import doobie.util.transactor.Transactor

class QuoteRepo[F[_] : Sync](xa: Transactor[F]) {

  val me: MonadError[F, Throwable] = implicitly[MonadError[F, Throwable]]

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

  def listQuotees(): F[List[Quote]] = Quote.Dao.list().to[List].transact(xa)

  def findQuote(id: Long): F[Option[Quote]] = Quote.Dao.find(id).option.transact(xa)

}