package gg.athleets.http

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import gg.athleets.repositories.UserRepository.User
import play.api.libs.json.{Json, Writes}

trait HttpProtocol extends PlayJsonSupport {
  implicit val UserJsonReads = Json.using[Json.WithDefaultValues].reads[User]
  implicit val UserJsonWrites: Writes[User] = Writes { user => Json.obj(
    "id" -> user._id,
    "username" -> user.username,
  )}
}
