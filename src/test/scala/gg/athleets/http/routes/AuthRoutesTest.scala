package gg.athleets.http.routes

import java.util.UUID

import akka.http.scaladsl.model.{FormData, StatusCodes}
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Route}
import gg.athleets.http.HttpProtocol
import gg.athleets.{ServiceComponents, ServiceTest}
import play.api.libs.json._

class AuthRoutesTest extends ServiceTest with ServiceComponents with HttpProtocol {
  val routes = httpServer.routes
  final case class RegisterData(username: String, password: String)
  implicit val RegisterDataJsonFormat = Json.format[RegisterData]
  implicit val AuthTokenResponseJsonFormat = Json.format[AuthTokenResponse]

  "should register and authenticate users" in {
    val email = s"user-${UUID.randomUUID}"
    val password = "password"
    val authData = FormData("grant_type" -> "password", "username" -> email, "password" -> password)
    val invalidAuthData = FormData("grant_type" -> "password", "username" -> email, "password" -> s"$password-wrong")
    val invalidRefreshData = FormData("grant_type" -> "refresh_token", "refresh_token" -> "")
    val registerData = RegisterData(username = email, password = password)

    Post("/auth/token/create", authData) ~> routes ~> check {
      rejection should be(a[AuthenticationFailedRejection])
    }
    Post("/users/register", Json.toJson(registerData)) ~> routes ~> check {
      status should be(StatusCodes.Created)
    }
    Post("/auth/token/create", invalidAuthData) ~> routes ~> check {
      rejection should be(a[AuthenticationFailedRejection])
    }
    Post("/auth/token/create", invalidRefreshData) ~> routes ~> check {
      rejection should be(a[AuthenticationFailedRejection])
    }
    Post("/auth/token/create", authData) ~> routes ~> check {
      status should be(StatusCodes.OK)
      val authTokenResponse = responseAs[AuthTokenResponse]
      val refreshData = FormData("grant_type" -> "refresh_token", "refresh_token" -> authTokenResponse.refresh_token)
      Post("/auth/token/create", refreshData) ~> routes ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }

  "should fail to register two users with the same email" in {
    val email = s"user-${UUID.randomUUID}"
    val password = "password"
    val registerData = RegisterData(username = email, password = password)

    Post("/users/register", Json.toJson(registerData)) ~> routes ~> check {
      status should be(StatusCodes.Created)
    }
    Post("/users/register", Json.toJson(registerData)) ~> routes ~> check {
      status should be(StatusCodes.Conflict)
    }
  }
}
