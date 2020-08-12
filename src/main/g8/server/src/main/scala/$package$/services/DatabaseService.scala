package $package$.services

import $package$.models.ExampleDAO
import $package$.modules.database.Database.DAO
import $package$.models.SettingsModels
import $package$.models.SettingsModels.{Database => DbName, _}
import org.mongodb.scala.MongoClient
import zio.Task
import zio.ZIO

object DatabaseService {

  def build(
    mongoClient: MongoClient,
    createMongoIndexes: Concurrency,
    database: DbName,
    databasePrefix: Option[String]
  ): Task[DAO] =
    for {
      fullDbName <- buildDbName(database, databasePrefix)
      exampleDAO <- ExampleDAO(mongoClient, createMongoIndexes, fullDbName)
    } yield {
      DAO(exampleDAO)
    }

  private def buildDbName(
    database: DbName,
    databasePrefix: Option[String]
  ): Task[DbName] =
    databasePrefix
      .map(prefix => ZIO.fromEither(SettingsModels.toDatabase(s"\$prefix\$database")))
      .getOrElse(ZIO.succeed(database))
      .mapError(new RuntimeException(_))
}
