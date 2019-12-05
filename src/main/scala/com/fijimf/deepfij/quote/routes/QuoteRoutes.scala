package com.fijimf.deepfij.quote.routes

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.quote.services.QuoteRepo
import com.fijimf.deepfij.quote.util.ServerInfo
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import org.slf4j.{Logger, LoggerFactory}

object QuoteRoutes {
  val log: Logger = LoggerFactory.getLogger(QuoteRoutes.getClass)

  def routes[F[_]](repo: QuoteRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {

    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    def findQuote(id: Long): F[Response[F]] = {
      (for {
        mq <- repo.findQuote(id)
        resp <- mq match {
          case Some(q) => Ok(q)
          case None => NotFound()
        }
      } yield {
        resp
      }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
    }

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
      case GET -> Root / "quote" / LongVar(id) =>
        findQuote(id)
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