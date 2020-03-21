package com.fijimf.deepfij.quote.model

import cats.Applicative
import cats.effect.Sync
import doobie.implicits._
import doobie.util.update.Update0
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}


case class Quote(id: Long, text: String, source: String, tag: Option[String], link: Option[String]) {

}

object Quote {

  implicit val quoteEncoder: Encoder.AsObject[Quote] = deriveEncoder[Quote]
  implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]

  implicit def quoteEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Quote] = jsonEncoderOf

  implicit def quoteEntityDecoder[F[_] : Sync]: EntityDecoder[F, Quote] = jsonOf

  implicit def lstQuoteEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Quote]] = jsonEncoderOf

  implicit def lstQuoteEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Quote]] = jsonOf

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