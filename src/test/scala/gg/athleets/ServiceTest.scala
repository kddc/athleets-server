package gg.athleets

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import gg.athleets.components.{HttpClientComponentsBase, ServiceComponentsBase}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

abstract class ServiceTest
  extends WordSpec
    with ScalatestRouteTest
    with ServiceComponentsBase
    with HttpClientComponentsBase
    with TestMongoDbComponents
    with Matchers
    with ScalaFutures
    with Eventually {
  override def createActorSystem() = ActorSystem()
  override implicit lazy val actorSystem = system
  override implicit lazy val executionContext = executor
  override lazy val config = actorSystem.settings.config
  override lazy val sttpBackend = AkkaHttpBackend.usingActorSystem(actorSystem)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(15, Seconds), interval = Span(10, Millis))
  implicit val timeout = RouteTestTimeout(15.seconds)
}
