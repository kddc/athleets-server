package gg.athleets.utils

import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID

import reactivemongo.bson.Subtype.UuidSubtype
import reactivemongo.bson.{ BSONBinary, BSONDateTime, BSONLong, BSONReader, BSONValue, BSONWriter }

trait BaseBSONProtocol {
  implicit val uuidReaderWriter = new BSONReader[BSONBinary, UUID] with BSONWriter[UUID, BSONBinary] {
    override def write(uuid: UUID): BSONBinary = {
      val bb = ByteBuffer.wrap(new Array[Byte](16))
      bb.putLong(uuid.getMostSignificantBits)
      bb.putLong(uuid.getLeastSignificantBits)
      BSONBinary(bb.array, UuidSubtype)
    }
    override def read(bson: BSONBinary): UUID = {
      val bb = ByteBuffer.wrap(bson.byteArray)
      new UUID(bb.getLong, bb.getLong)
    }
  }

  implicit val instantReaderWriter = new BSONReader[BSONValue, Instant] with BSONWriter[Instant, BSONDateTime] {
    override def write(t: Instant): BSONDateTime = BSONDateTime(t.toEpochMilli)
    override def read(bson: BSONValue): Instant = bson match {
      case BSONDateTime(value) => Instant.ofEpochMilli(value)
      case BSONLong(value) => Instant.ofEpochMilli(value)
      case _ => throw new RuntimeException()
    }
  }
}
