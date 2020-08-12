package $package$.models

import $package$.RoutesModels.MongoError
import $package$.extensions.MongoModelExtensions._
import $package$.models.ExampleModels.Example
import $package$.models.CommonDAO.observableToStream
import $package$.models.SettingsModels.Concurrency
import $package$.models.SettingsModels.Database
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.IndexOptions
import org.mongodb.scala.model.Indexes
import zio.Task
import zio.stream.Stream

object ExampleModels {

  case class Example(
    id: String,
    name: String)
}

object ExampleDAO extends CommonDAO {
  def apply(
    mongoClient: MongoClient,
    createMongoIndexes: Concurrency,
    database: Database
  ): Task[ExampleDAO] =
    getCollection[Example](
      mongoClient,
      createMongoIndexes,
      database,
      "example",
      fromProviders(classOf[Example])
    )(
      Indexes.ascending("id") -> IndexOptions().unique(true)
    ).map { case (coll, _) => ExampleDAO(coll) }
}

case class ExampleDAO(collection: MongoCollection[Example]) {

  def findById(id: String): Stream[MongoError, Example] =
    collection
      .find("id" === id)
      .limit(1)

}
