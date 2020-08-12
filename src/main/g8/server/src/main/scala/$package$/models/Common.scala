package $package$.models

import $package$.RoutesModels.MongoError
import $package$.log.Logger.log
import $package$.models.SettingsModels.Concurrency
import $package$.models.SettingsModels.Database
import $package$.mongodb.ZioMongoExtensions._
import eu.timepit.refined.auto._
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.Observable
import org.mongodb.scala.model.IndexOptions
import scala.reflect.ClassTag
import zio.Task
import zio.ZIO
import zio.stream.Stream

trait CommonDAO {

  protected def getCollection[A](
    client: MongoClient,
    createMongoIndexes: Concurrency,
    database: Database,
    name: String,
    registry: CodecRegistry,
    oldIndices: String*
  )(newIndices: (Bson, IndexOptions)*
  )(implicit ct: ClassTag[A]
  ): Task[(MongoCollection[A], CodecRegistry)] = {
    val fullRegistry = fromRegistries(registry, DEFAULT_CODEC_REGISTRY)
    val collection   = client.getDatabase(database).withCodecRegistry(fullRegistry).getCollection[A](name)
    for {
      indices       <- collection.listIndexes().toStream.all.map(_.map(_.getString("name")))
      indicesToDrop = oldIndices intersect indices
      _ <- ZIO.foreachParN(createMongoIndexes)(indicesToDrop)(key =>
            collection
              .dropIndex(key)
              .toStream
              .head(new RuntimeException(s"index deletion \$key on \${collection.namespace} failed"))
          )
      _ <- ZIO.when(indicesToDrop.nonEmpty)(
            log.info(s"index deleted on \${collection.namespace}: \${oldIndices.mkString(", ")}")
          )

      createResults <- ZIO.foreachParN(createMongoIndexes)(newIndices) {
                        case (key, options) =>
                          collection
                            .createIndex(key, options)
                            .toStream
                            .head(
                              new RuntimeException(s"index creation \$key/\$options on \${collection.namespace} failed")
                            )
                      }
      _ <- log.info(s"index created on \${collection.namespace}: \${createResults.mkString(", ")}")
    } yield {
      collection -> fullRegistry
    }
  }
}

object CommonDAO {

  implicit def observableToStream[A](observable: Observable[A]): Stream[MongoError, A] =
    observable.toStream(MongoError.fromThrowable)
}
