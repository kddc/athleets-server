package gg.athleets.utils

import gg.athleets.repositories.UserRepository.User
import pdi.jwt.exceptions.{JwtExpirationException, JwtValidationException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader}
import play.api.libs.json._

import scala.util.{Failure, Success}
import scala.concurrent.duration._

final case class ValidationFailure(error: String, description: String)

object JsonWebToken {
  import pdi.jwt.JwtJson._
  sealed trait Typ
  case object AccessToken extends Typ {
    override def toString(): String = "Access"
  }
  case object RefreshToken extends Typ {
    override def toString(): String = "Refresh"
  }

  implicit val UserJsonWrites: Writes[User] = Writes { user => Json.obj(
    "id" -> user._id,
    "username" -> user.username,
  )}
  val secret = "secretKey"
//  val issuer = "athleets.io"
  val realm = "athleets"

  def createToken(user: User, typ: Typ, lifeTime: FiniteDuration = 300.seconds) = {
    val token = Jwt.encode(
      JwtHeader(JwtAlgorithm.HS256),
      JwtClaim(Json.toJson(user).toString)
        .issuedNow
        .expiresIn(lifeTime.toSeconds)
        .+("typ", typ.toString),
      secret
    )
    token
  }

  def validateToken(token: String): Either[ValidationFailure, JwtClaim] = {
    Jwt.decode(token, secret, Seq(JwtAlgorithm.HS256)) match {
      case Success(claim) =>
        Right(Json.fromJson[JwtClaim](Json.parse(claim)).get)
      case Failure(_: JwtValidationException) =>
        Left(ValidationFailure("invalid_token", "The provided token is malformed"))
      case Failure(_: JwtExpirationException) =>
        Left(ValidationFailure("invalid_token", "The provided token expired"))
      case Failure(_) =>
        Left(ValidationFailure("invalid_token", ""))
    }
  }

  def validateRefreshToken(token: String): Either[ValidationFailure, JwtClaim] = {
    validateToken(token) match {
      case Right(jwtClaim) => {
        (Json.parse(jwtClaim.toJson) \ "typ").getOrElse(JsNull).as[String] match {
          case "Refresh" =>
            Right(jwtClaim)
          case _ =>
            Left(ValidationFailure("invalid_token", "The provided token must be a refresh token"))
        }
      }
      case Left(failure) => Left(failure)
    }
  }
}
