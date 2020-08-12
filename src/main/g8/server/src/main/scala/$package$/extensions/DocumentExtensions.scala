package $package$.extensions

import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.Encoder
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.BsonTransformer
import scala.util.chaining._

object DocumentExtensions {

  implicit class ConvertToDocument(any: Any) {

    def toDocument(implicit registry: CodecRegistry): BsonDocument =
      new BsonDocument().tap { doc =>
        val codec = registry.get(any.getClass).asInstanceOf[Encoder[Any]]
        codec.encode(new BsonDocumentWriter(doc), any, EncoderContext.builder().build())
      }

  }

  implicit class BsonTransformerProvider[A](a: Class[A]) {

    def bsonTransformer(implicit registry: CodecRegistry): BsonTransformer[A] =
      new BsonTransformer[A] {
        def apply(value: A): BsonDocument = value.toDocument
      }
  }
}
