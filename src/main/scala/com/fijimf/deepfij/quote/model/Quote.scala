package com.fijimf.deepfij.quote.model

import doobie.implicits._
import doobie.util.update.Update0

case class Quote(id: Long, text: String, source: String, tag: Option[String], link: Option[String]) {

}

object Quote {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "text", "source", "tag", "link")
    val tableName: String = "quote"

    def insert(q: Quote): Update0 =
      (fr"INSERT INTO quote(text, source,tag,link) VALUES (${q.text},${q.source},${q.tag},${q.link}) RETURNING" ++ colFr).update

    def update(q: Quote): Update0 =
      (fr""" UPDATE quote SET text = ${q.text}, source=${q.source}, tag = ${q.tag}, link=${q.link} WHERE id=${q.id}
             RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Quote] = (baseQuery ++ fr" WHERE id = $id").query[Quote]

    def list(): doobie.Query0[Quote] = baseQuery.query[Quote]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM quote where id=${id}".update

  }

}