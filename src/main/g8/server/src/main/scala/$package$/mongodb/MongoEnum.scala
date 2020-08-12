package $package$.mongodb

import enumeratum.Enum
import enumeratum.EnumEntry
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonTransformer
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

trait MongoEnum[A <: EnumEntry] { self: Enum[A] =>
  def className: Class[A]

  implicit val transform: BsonTransformer[A] =
    (value: A) => BsonString(value.entryName)

  private def codec: Codec[A] = new Codec[A] {
    override val getEncoderClass: Class[A] = className

    override def encode(
      writer: BsonWriter,
      parent: A,
      encoderContext: EncoderContext
    ): Unit = writer.writeString(parent.entryName)

    override def decode(
      reader: BsonReader,
      decoderContext: DecoderContext
    ): A = withName(reader.readString)
  }

  val codecProvider: CodecProvider = new CodecProvider {
    def get[T](
      clazz: Class[T],
      registry: CodecRegistry
    ): Codec[T] = {
      // this is hackish:
      if (clazz.getName.startsWith(className.getName)) codec.asInstanceOf[Codec[T]]
      else null
    }
  }
}
