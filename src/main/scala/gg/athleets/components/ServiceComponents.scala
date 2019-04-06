package gg.athleets.components

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

trait ServiceComponentsBase {
  implicit def actorSystem: ActorSystem
  implicit def executionContext: ExecutionContext
  implicit def materializer: ActorMaterializer
  implicit def config: Config
}

trait DefaultServiceComponents extends ServiceComponentsBase {
  implicit lazy val actorSystem = ActorSystem("athleets")
  implicit lazy val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
  implicit lazy val config: Config = actorSystem.settings.config
}
