package gg.athleets.providers

import java.util.UUID

import akka.http.scaladsl.model.headers.{ HttpChallenge, OAuth2BearerToken }
import akka.http.scaladsl.server.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ AuthenticationFailedRejection, Directive1 }
import com.github.t3hnar.bcrypt._
import gg.athleets.http.routes.AuthFailure
import gg.athleets.repositories.UserRepository
import gg.athleets.repositories.UserRepository.User
import gg.athleets.utils.JsonWebToken
import play.api.libs.json.Json
import com.typesafe.config.Config

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

class AuthProvider(userRepository: UserRepository)(implicit ex: ExecutionContext, config: Config) {
  type AuthResult[P] = Either[AuthFailure, P]
  val InvalidCredentials = AuthFailure("invalid_credentials", "Invalid Credentials")
  val InvalidToken = AuthFailure("invalid_token", "Could not find user provided in the token")

  //  def extractPrincipal

  def extractToken: Directive1[Option[String]] = {
    extractCredentials.flatMap {
      case Some(OAuth2BearerToken(token)) =>
        provide(Some(token))
      case _ =>
        provide(None)
    }
  }

  def extractUser: Directive1[User] = {
    extractToken.flatMap {
      case Some(token) => {
        JsonWebToken.validateToken(token) match {
          case Right(jwtClaim) => {
            val userId = (Json.parse(jwtClaim.toJson) \ "id").as[UUID]
            onSuccess(userRepository.findById(userId)).flatMap {
              case Some(user) =>
                provide(user)
              case None =>
                reject(AuthenticationFailedRejection(CredentialsRejected, HttpChallenge("Bearer", "athleets", Map("error" -> InvalidToken.error, "error_description" -> InvalidToken.description))))
            }
          }
          case Left(failure) =>
            reject(AuthenticationFailedRejection(CredentialsRejected, HttpChallenge("Bearer", "athleets", Map("error" -> failure.error, "error_description" -> failure.description))))
        }
      }
      case None => reject(AuthenticationFailedRejection(CredentialsMissing, HttpChallenge("Bearer", "athleets")))
    }
  }

  def verifyUserCredentials(username: String, password: String): Future[AuthResult[User]] = {
    userRepository.findByUsername(username).map {
      case Some(user) => {
        password.isBcryptedSafe(user.password) match {
          case Success(true) => Right(user)
          case _ => Left(InvalidCredentials)
        }
      }
      case None => Left(InvalidCredentials)
    }
  }

  def verifyRefreshToken(token: String): Future[AuthResult[User]] = {
    JsonWebToken.validateRefreshToken(token) match {
      case Right(jwtClaim) => {
        val userId = (Json.parse(jwtClaim.toJson) \ "id").as[UUID]
        userRepository.findById(userId).map {
          case Some(user) => Right(user)
          case None => Left(InvalidToken)
        }
      }
      case Left(failure) =>
        Future.successful(Left(AuthFailure(failure.error, failure.description)))
    }
  }
}