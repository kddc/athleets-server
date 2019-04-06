package gg.athleets.components

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend

import scala.concurrent.Future

trait HttpClientComponentsBase {
  implicit def sttpBackend: SttpBackend[Future, Nothing]
}

trait DefaultHttpClientComponents extends HttpClientComponentsBase {
  this: ServiceComponentsBase =>
  override implicit lazy val sttpBackend: SttpBackend[Future, Nothing] = AkkaHttpBackend.usingActorSystem(actorSystem)
}
