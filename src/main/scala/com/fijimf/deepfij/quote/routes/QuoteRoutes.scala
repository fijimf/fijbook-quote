package com.fijimf.deepfij.quote.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import org.http4s.circe.jsonEncoderOf
import com.fijimf.deepfij.quote.services.QuoteRepo
import com.fijimf.deepfij.quote.util.ServerInfo
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.slf4j.{Logger, LoggerFactory}

object QuoteRoutes {
  val log: Logger = LoggerFactory.getLogger(QuoteRoutes.getClass)

  def routes[F[_]](repo: QuoteRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {

    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    import com.fijimf.deepfij.quote.model._
    implicit def intEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Int] = jsonEncoderOf


    HttpRoutes.of[F] {
      case GET -> Root / "random" =>
        for {
          q<-repo.randomQuote()
          resp<-Ok(q)
        } yield {
          resp
        }
      case GET -> Root /"random"/ tag=>
        for {
          q<-repo.randomQuote(tag, 0.25)
          resp<-Ok(q)
        } yield {
          resp
        }

      case GET -> Root / "quote" =>
        (for {
          quotes <- repo.listQuotes()
          resp <- Ok(quotes)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
      case GET -> Root / "quote" / LongVar(id) =>
        (for {
          quote <- repo.findQuote(id)
          resp <- quote match {
            case Some(q) => Ok(q)
            case None => NotFound()
          }
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
      case req@POST -> Root / "quote" =>
        (for {
          q <- req.as[Quote]
          x <- q.id match {
            case 0 => repo.insertQuote(q)
            case _ => repo.updateQuote(q)
          }
          resp <- Ok(x)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
      case DELETE -> Root / "quote" / LongVar(id) =>
        (for {
          n <- repo.deleteQuote(id)
          resp <- Ok(n)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
    }
  }

  def healthcheckRoutes[F[_]](repo: QuoteRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "status" =>
        for {
          status <- repo.healthcheck.map(isOk => ServerInfo.fromStatus(isOk))
          resp <- if (status.isOk) Ok(status) else InternalServerError(status)
        } yield {
          resp
        }
    }
  }


}