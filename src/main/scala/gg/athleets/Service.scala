package gg.athleets

import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import gg.athleets.components._
import gg.athleets.http.HttpServer
import gg.athleets.http.routes.{AuthRoutes, UserRoutes}
import gg.athleets.providers.AuthProvider
import gg.athleets.repositories.{UserRepository}

import scala.util.{Failure, Success}

trait ServiceComponents {
  this: ServiceComponentsBase with MongoDbComponentsBase with HttpClientComponentsBase =>
  lazy val userRepository = new UserRepository(mongoDb)

  implicit lazy val authProvider = new AuthProvider(userRepository)
  lazy val authRoutes = new AuthRoutes()
  lazy val userRoutes = new UserRoutes(userRepository)

  lazy val httpServer = new HttpServer(
    authRoutes = authRoutes,
    userRoutes = userRoutes,
  )
}

class Service
  extends ServiceComponents
  with DefaultServiceComponents
  with DefaultMongoDbComponents
  with DefaultHttpClientComponents
  with LazyLogging {

  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")

  def start() = {
    Http().bindAndHandle(httpServer.routes, interface, port).onComplete {
      case Success(binding) =>
        logger.info(s"Successfully bound to ${binding.localAddress}")
      case Failure(error) =>
        logger.error("Binding failed", error)
        System.exit(1)
    }
  }
}
