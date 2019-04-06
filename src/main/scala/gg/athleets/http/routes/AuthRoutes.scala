package gg.athleets.http.routes

import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.{ AuthenticationFailedRejection }
import akka.http.scaladsl.server.AuthenticationFailedRejection.{ CredentialsRejected }
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import gg.athleets.http.{ HttpProtocol, HttpRoutes }
import gg.athleets.providers.AuthProvider
import gg.athleets.repositories.UserRepository.User
import gg.athleets.utils.JsonWebToken
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

final case class AuthTokenResponse(
  access_token: String,
  refresh_token: String,
  token_type: String,
  expires_in: Long)

final case class AuthFailure(
  error: String,
  description: String)

class AuthRoutes(implicit ec: ExecutionContext, authProvider: AuthProvider) extends HttpRoutes
  with HttpProtocol
  with LazyLogging {
  implicit val AuthTokenResponseJsonFormat = Json.format[AuthTokenResponse]

  def routes = path("auth" / "token" / "create") {
    post {
      formField('grant_type.as[String]) {
        case "password" => {
          formFields('username, 'password) {
            case (username, password) =>
              onSuccess(passwordGrant(username, password)) {
                case Right(authTokenResponse) => complete(authTokenResponse)
                case Left(authRejection) => reject(authRejection)
              }
          }
        }
        case "refresh_token" => {
          formField('refresh_token.as[String]) { refreshToken =>
            onSuccess(refreshTokenGrant(refreshToken)) {
              case Right(authTokenResponse) => complete(authTokenResponse)
              case Left(authRejection) => reject(authRejection)
            }
          }
        }
      }
    }
  }

  private def passwordGrant(username: String, password: String): Future[Either[AuthenticationFailedRejection, AuthTokenResponse]] = {
    authProvider.verifyUserCredentials(username, password).map {
      case Right(user) => Right(createAuthTokenResponse(user))
      case Left(failure) => Left(createRejection(failure))
    }
  }

  private def refreshTokenGrant(token: String): Future[Either[AuthenticationFailedRejection, AuthTokenResponse]] = {
    authProvider.verifyRefreshToken(token).map {
      case Right(user) => Right(createAuthTokenResponse(user))
      case Left(failure) => Left(createRejection(failure))
    }
  }

  private def createAuthTokenResponse(user: User): AuthTokenResponse = {
    val accessTokenLifeTime = 300.seconds
    val refreshTokenLifeTime = 30.days
    val accessToken = JsonWebToken.createToken(user, JsonWebToken.AccessToken, accessTokenLifeTime)
    val refreshToken = JsonWebToken.createToken(user, JsonWebToken.RefreshToken, refreshTokenLifeTime)
    AuthTokenResponse(accessToken, refreshToken, "Bearer", accessTokenLifeTime.toSeconds - 1)
  }

  private def createRejection(failure: AuthFailure): AuthenticationFailedRejection = {
    AuthenticationFailedRejection(CredentialsRejected, HttpChallenge("Bearer", JsonWebToken.realm, Map("error" -> failure.error, "error_description" -> failure.description)))
  }
}