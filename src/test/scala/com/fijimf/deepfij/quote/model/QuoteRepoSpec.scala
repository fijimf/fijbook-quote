package com.fijimf.deepfij.quote.model

import cats.effect.IO
import com.fijimf.deepfij.quote.services.QuoteRepo
import doobie.implicits._

class QuoteRepoSpec extends DbIntegrationSpec {
  val containerName = "quote-repo-spec"
  val port = "7375"

  describe("Quote repo ops") {
    val repo = new QuoteRepo[IO](transactor)
    val newQuote1: Quote = Quote(0L, "QUOTE 1", "###", None, None)
    val newQuote2: Quote = Quote(0L, "QUOTE 2", "$$$", Some("tag"), None)
    describe("Quote ops") {
      it("should list all quotees") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          quoteList <- repo.listQuotes()
        } yield {
          assert(quoteList.size === 0)
        }).unsafeRunSync()
      }

      it("should insert a quote") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote1)
          quoteList0 <- repo.listQuotes()
          b <- repo.insertQuote(newQuote2)
          quoteList1 <- repo.listQuotes()
        } yield {
          assert(a.id > 0L)
          assert(b.id > 0L)
          assert(!(a.id === b.id))
          assert(quoteList1.size >= quoteList0.size)
          assert(quoteList1.contains(a))
          assert(quoteList1.contains(b))
        }).unsafeRunSync()
      }

      it("should find quotes") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote1)
          b <- repo.insertQuote(newQuote2)
          a1 <- repo.findQuote(a.id)
          _ <- repo.findQuote(b.id)
          ax <- repo.findQuote(-999L)
        } yield {
          assert(a1 === Some(a))
          assert(ax.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a quote") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote1)
          _ <- repo.insertQuote(newQuote2)
          a1 <- repo.findQuote(a.id)
          a2 <- repo.updateQuote(a.copy(text = "X-X-X-X"))
          a3 <- repo.findQuote(a.id)
        } yield {
          assert(a1 === Some(a))
          assert(!(a === a2))
          assert(a3 === Some(a2))
        }).unsafeRunSync()
      }

      it("should delete an quote") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote2)
          list1 <- repo.listQuotes()
          n <- repo.deleteQuote(a.id)
          list2 <- repo.listQuotes()
          m <- repo.deleteQuote(-999L)
        } yield {
          assert(list1.contains(a))
          assert(n === 1)
          assert(!list2.contains(a))
          assert(m === 0)
        }).unsafeRunSync()
      }

      it("should serve a random quote") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote1)
          b <- repo.insertQuote(newQuote2)
          a1 <- repo.randomQuote()
          a2 <- repo.randomQuote()
          a3 <- repo.randomQuote()
        } yield {
          assert(a1.text === "QUOTE 1" || a1.text === "QUOTE 2" )
          assert(a2.text === "QUOTE 1" || a2.text === "QUOTE 2" )
          assert(a3.text === "QUOTE 1" || a3.text === "QUOTE 2" )
        }).unsafeRunSync()
      }

      it("should serve a random keyed quote") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a <- repo.insertQuote(newQuote1)
          b <- repo.insertQuote(newQuote2)
          a1 <- repo.randomQuote("tag", 1.0)
          a2 <- repo.randomQuote("tag", 1.0)
          a3 <- repo.randomQuote("missing-tag", 1.0)
        } yield {
          assert(a1.text === "QUOTE 2" )
          assert(a2.text === "QUOTE 2" )
          assert(a3.text === "QUOTE 1" || a3.text === "QUOTE 2" )
        }).unsafeRunSync()
      }

      it("should serve a random quote even empty") {
        (for {
          _ <- Quote.Dao.truncate().run.transact(transactor)
          a1 <- repo.randomQuote("tag", 1.0)
          a2 <- repo.randomQuote()
          a3 <- repo.randomQuote("missing-tag", 1.0)
        } yield {
          assert(a1.text === "No quotes loaded" )
          assert(a2.text === "No quotes loaded" )
          assert(a3.text === "No quotes loaded" )
        }).unsafeRunSync()
      }
    }
  }
}