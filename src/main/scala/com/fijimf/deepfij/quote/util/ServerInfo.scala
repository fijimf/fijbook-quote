package com.fijimf.deepfij.quote.util


import cats.Applicative
import cats.effect.Sync
import com.fijimf.deepfij.quote.BuildInfo
import com.fijimf.deepfij.quote.model.Quote
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class ServerInfo(name: String, version: String, scalaVersion: String, sbtVersion: String, buildNumber: Int, builtAt: String, isOk: Boolean)

case object ServerInfo {

  implicit val healthyEncoder: Encoder.AsObject[ServerInfo] = deriveEncoder[ServerInfo]
  implicit def healthyEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ServerInfo] = jsonEncoderOf
  implicit val healthyDecoder: Decoder[ServerInfo] = deriveDecoder[ServerInfo]
  implicit def healthyEntityDecoder[F[_] : Sync]: EntityDecoder[F, ServerInfo] = jsonOf



def fromStatus(status: Boolean): ServerInfo = {
    ServerInfo(
      BuildInfo.name,
      BuildInfo.version,
      BuildInfo.scalaVersion,
      BuildInfo.sbtVersion,
      BuildInfo.buildInfoBuildNumber,
      BuildInfo.builtAtString,
      status)
  }
}