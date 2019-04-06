package gg.athleets.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.scalalogging.LazyLogging
import gg.athleets.http.{HttpProtocol, HttpRoutes}
import gg.athleets.repositories.UserRepository
import gg.athleets.repositories.UserRepository._
import com.github.t3hnar.bcrypt._
import gg.athleets.providers.AuthProvider

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class UserRoutes(userRepository: UserRepository)(implicit executionContext: ExecutionContext, materializer: ActorMaterializer, authProvider: AuthProvider)
  extends HttpRoutes
  with HttpProtocol
  with LazyLogging {

  def routes = concat(
    pathPrefix("users")(
      concat(
        registerUserRoute,
        listUsersRoute,
        retrieveUserRoute,
      )
    ),
    pathPrefix("profile")(
      concat(retrieveProfileRoute)
    )
  )

  def registerUserRoute = path("register") {
    post {
      entity(as[User]) { createUser =>
        onComplete(userRepository.create(createUser.copy(password = createUser.password.bcrypt))) {
          case Success(user) => complete(StatusCodes.Created, user)
          case Failure(_) => complete(StatusCodes.Conflict, "User could not be created")
        }
      }
    }
  }

  def listUsersRoute = pathEnd {
    get {
      withAuthorization { _ =>
        val usersF = userRepository.all.runWith(Sink.collection)
        onSuccess(usersF) { users =>
          complete(users)
        }
      }
    }
  }

  def retrieveUserRoute = path(JavaUUID) { id =>
    get {
      onSuccess(userRepository.findById(id)) {
        case Some(user) => complete(user)
        case None => reject
      }
    }
  }

  def retrieveProfileRoute = pathEnd {
    get {
      withAuthorization { user => complete(user) }
    }
  }

}
