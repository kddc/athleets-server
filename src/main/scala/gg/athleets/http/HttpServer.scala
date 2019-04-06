package gg.athleets.http

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import gg.athleets.http.routes.{AuthRoutes, UserRoutes}

import scala.concurrent.ExecutionContext

class HttpServer(
  authRoutes: AuthRoutes,
  userRoutes: UserRoutes,
)(implicit executionContext: ExecutionContext, materializer: ActorMaterializer) {

  def routes = concat(
    authRoutes.routes,
    userRoutes.routes,
  )
}
