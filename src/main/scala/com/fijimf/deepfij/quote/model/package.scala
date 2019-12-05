package com.fijimf.deepfij.quote

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object model {
  implicit val quoteEncoder: Encoder.AsObject[Quote] = deriveEncoder[Quote]
  implicit val quoteDecoder: Decoder[Quote] = deriveDecoder[Quote]

  implicit def quoteEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Quote] = jsonEncoderOf

  implicit def quoteEntityDecoder[F[_] : Sync]: EntityDecoder[F, Quote] = jsonOf

  implicit def lstQuoteEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Quote]] = jsonEncoderOf

  implicit def lstQuoteEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Quote]] = jsonOf
}
