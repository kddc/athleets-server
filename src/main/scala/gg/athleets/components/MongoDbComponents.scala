package gg.athleets.components

import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.Await
import scala.concurrent.duration._

trait MongoDbComponentsBase {
  def mongoDbDatabaseName: String
  implicit def mongoDb: DefaultDB
}

trait DefaultMongoDbComponents extends MongoDbComponentsBase {
  this: ServiceComponentsBase =>
  private lazy val uri = MongoConnection.parseURI(config.getString("mongodb.uri")).get
  private lazy val driver = MongoDriver(config)
  private lazy val connection = driver.connection(uri)
  lazy val mongoDbDatabaseName = config.getString("mongodb.database-name")
  implicit lazy val mongoDb = Await.result(connection.database(mongoDbDatabaseName), 10.seconds)
}
