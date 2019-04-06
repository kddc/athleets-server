package gg.athleets.repositories

import java.util.UUID

import akka.Done
import akka.stream.scaladsl.Source
import gg.athleets.utils.BaseBSONProtocol
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{ BSONDocument, BSONDocumentHandler, Macros }
import reactivemongo.akkastream._

import scala.concurrent.{ Await, Future }
import akka.stream.ActorMaterializer
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{ Index, IndexType }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class UserRepository(db: DefaultDB)(implicit ex: ExecutionContext, mat: ActorMaterializer) {
  import UserRepository.BSONProtocol._
  import UserRepository._

  private val collection = Await.result(init(db), 10.seconds)

  def all: Source[User, Future[State]] = {
    collection
      .find(BSONDocument.empty)
      .cursor[User]()
      .documentSource()
  }

  def findById(id: UUID): Future[Option[User]] = {
    collection.find(BSONDocument("_id" -> id)).one[User]
  }

  def findByUsername(name: String): Future[Option[User]] = {
    collection.find(BSONDocument("username" -> name)).one[User]
  }

  def create(user: User): Future[User] = {
    collection.insert(user).map(_ => user)
  }
}

object UserRepository {
  final case class User(_id: UUID = UUID.randomUUID, username: String, password: String)

  private def init(db: DefaultDB)(implicit ec: ExecutionContext): Future[BSONCollection] = {
    val collectionName = "users"
    val collection = db.collection[BSONCollection](collectionName)
    for {
      collections <- db.collectionNames
      _ <- if (!collections.contains(collection.name)) for {
        _ <- collection.create()
        _ <- collection.indexesManager.ensure(Index(List("username" -> IndexType.Ascending), unique = true))
      } yield Done
      else Future.successful(Done)
    } yield collection
  }

  object BSONProtocol extends BaseBSONProtocol {
    implicit val UserHandler: BSONDocumentHandler[User] = Macros.handler[User]
  }
}