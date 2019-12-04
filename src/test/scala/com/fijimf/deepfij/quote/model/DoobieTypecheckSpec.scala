package com.fijimf.deepfij.quote.model

import java.time.{LocalDate, LocalDateTime}

class DoobieTypecheckSpec extends DbIntegrationSpec {
  val containerName = "doobie-typecheck-spec"
  val port="7374"

  describe("Doobie typechecking Dao's") {
    describe("Quote.Dao") {
      it("insert should typecheck") {
        check(Quote.Dao.insert(Quote(0L,"Everybody got a plan until they get punched in the mouth","Mike Tyson",None,None)))
      }

      it("list should typecheck") {
        check(Quote.Dao.list())
      }

      it("find should typecheck") {
        check(Quote.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Quote.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Quote.Dao.update(Quote(99L,"Everybody got a plan until they get punched in the mouth","Mike Tyson",None,None)))
      }

      it("truncate should typecheck") {
        check(Quote.Dao.truncate())
      }
    }
  }
}
