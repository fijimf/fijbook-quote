package com.fijimf.deepfij.quote

import com.fijimf.deepfij.quote.model._
import cats.data.Kleisli
import cats.effect.{Effect, IO}
import cats.implicits.{ catsSyntaxEq => _, _ }
import com.fijimf.deepfij.quote.model.Quote
import com.fijimf.deepfij.quote.routes.QuoteRoutes
import com.fijimf.deepfij.quote.services.QuoteOperations
import com.fijimf.deepfij.quote.util.ServerInfo
import org.http4s.syntax.kleisli._
import org.http4s.{Method, Request, Response, Status, Uri}
import org.scalatest.FunSpec

class QuoteRoutesSpec extends FunSpec {
  val hp: QuoteOperations[IO] = new QuoteOperations[IO] {
    override def healthcheck: IO[Boolean] = IO {
      true
    }

    override def insertQuote(a: Quote): IO[Quote] = IO {
      a.copy(id=123L)
    }

    override def updateQuote(a: Quote): IO[Quote] = IO {
      a
    }

    override def deleteQuote(id: Long): IO[Int] = IO(1)

    override def listQuotes(): IO[List[Quote]] =
      IO {
        List(
          Quote(1L, "text1", "source", None, None),
          Quote(2L, "text2", "source", None, None),
          Quote(3L, "text3", "source", None, None)
        )
      }

    override def findQuote(id: Long): IO[Option[Quote]] = IO {
      Some(Quote(3L, "text1", "source", None, None))
    }

    override def randomQuote(): IO[Quote] = IO {
      Quote(3L, "text3", "source", None, None)
    }

    override def randomQuote(tag: String, pct: Double): IO[Quote] = IO {
      Quote(3L, "text3Q", "source", None, None)
    }
  }

  def service[F[_]](repo: QuoteOperations[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] =
    (QuoteRoutes.routes(repo) <+> QuoteRoutes.healthcheckRoutes(repo)).orNotFound

  describe("Quote routes should handle operations in the happy path ") {


    it("/status") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/status"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[ServerInfo].map(_.isOk).unsafeRunSync())
    }
    it("/random") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/random"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Quote].map(_.text).unsafeRunSync()==="text3")
    }
    it("/random/with-a-tag") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/random/tag"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Quote].map(_.text).unsafeRunSync()==="text3Q")
    }
    it("/quote") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/quote"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[List[Quote]].map(_.size).unsafeRunSync()===3)
    }
    it("/quote/1") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/quote/1"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Quote].map(_.text).unsafeRunSync()==="text1")
    }

    it("/quote (POST - insert)") {
      val q: Quote = Quote(0L, "XXXX", "XXXX", None, None)
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/quote")).withEntity(q)
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Quote].unsafeRunSync()===q.copy(id=123L))
    }

    it("/quote (POST - update)") {
      val q: Quote = Quote(3L, "XXXX", "XXXX", None, None)
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/quote")).withEntity(q)
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Quote].unsafeRunSync()===q)
    }

    it("/quote/1 (DELETE)") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/quote/1"))
      val response: Response[IO] = service(hp).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }
  }


}
