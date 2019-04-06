package gg.athleets.http

import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection, Route}
import akka.http.scaladsl.server.Directives._
import gg.athleets.providers.AuthProvider
import gg.athleets.repositories.UserRepository.User

import scala.concurrent.Future

abstract class HttpRoutes(implicit authProvider: AuthProvider) extends HttpProtocol {
  def routes: Route

  def withAuthorization: Directive1[User] = {
    authProvider.extractUser
  }

  def onSuccessAndDefined[T](res: Future[Option[T]]): Directive1[T] = {
    onSuccess(res).flatMap {
      case Some(value) => provide(value)
      case None => reject
    }
  }

  def paging(defaultLimit: Int): Directive1[(Int, Int)] = {
    parameters('from.as[Int].?, 'limit.as[Int].?).tflatMap {
      case (from, _) if from.exists(_ < 0) =>
        reject(MalformedQueryParamRejection("from", "Must not be negative"))
      case (from, limit) =>
        provide((
          from.getOrElse(0),
          limit.getOrElse(defaultLimit)))
    }
  }
}
