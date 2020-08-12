package $package$.mongodb

import java.time.Instant
import org.mongodb.scala.bson.{ BsonDateTime, BsonTransformer }

object InstantBsonTransformer {

  implicit object TransformInstant extends BsonTransformer[Instant] {
    override def apply(value: Instant): BsonDateTime =
      BsonDateTime(value.toEpochMilli)
  }
}
