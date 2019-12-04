package com.fijimf.deepfij.quote

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import cats.syntax.semigroupk._
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}
import com.fijimf.deepfij.quote.services.QuoteRepo
import com.fijimf.deepfij.quote.util.Banner
import com.fijimf.deepfij.quote.routes.QuoteRoutes


object QuoteServer {

  @SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
  def stream[F[_] : ConcurrentEffect](transactor: Transactor[F], port:Int)(implicit T: Timer[F], C: ContextShift[F]): Stream[F, ExitCode] = {
    val repo: QuoteRepo[F] = new QuoteRepo[F](transactor)
    val healthcheckService: HttpRoutes[F] = QuoteRoutes.healthcheckRoutes(repo)
    val quoteService: HttpRoutes[F] = QuoteRoutes.routes(repo)
    val httpApp: HttpApp[F] = (healthcheckService <+> quoteService ).orNotFound
    val finalHttpApp: HttpApp[F] = Logger.httpApp[F](logHeaders = true, logBody = true)(httpApp)
    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(port = port, host = "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .withBanner(Banner.banner)
        .serve
    } yield {
      exitCode
    }
    }.drain
}
