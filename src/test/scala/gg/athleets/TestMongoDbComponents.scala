package gg.athleets

import java.util.UUID

import gg.athleets.components.{DefaultMongoDbComponents, ServiceComponentsBase}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Await
import scala.concurrent.duration._

trait TestMongoDbComponents extends BeforeAndAfterAll with DefaultMongoDbComponents {
    this: ServiceComponentsBase with Suite =>
  override lazy val mongoDbDatabaseName = "test_" + UUID.randomUUID().toString.replaceAll("-", "")

  override protected def afterAll(): Unit = {
    Await.result(mongoDb.drop(), 10.seconds)
    super.afterAll()
  }
}