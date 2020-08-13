package $package$.servers

import $package$.clients.MongoDbClient
import $package$.clients.MongoDbClient.DbClientEnv
import $package$.models.SettingsModels
import $package$.modules.database.Database
import $package$.modules.database.Database.DAO
import $package$.services.DatabaseService
import zio.Has
import zio.Managed
import zio.UIO
import zio.ULayer
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.Task

object DatabaseServer {

  def asLayer: ULayer[Database] =
    MongoDbServer.asLayer ++ MongoDbClient.asLayer >>> buildDatabase

  private def buildDatabase: URLayer[Has[DbClientEnv], Database] =
    ZLayer.fromServiceManaged { dbClient =>
      for {
        dao <- Managed.fromEffect(buildDAO(dbClient))
        db  <- Database.live(dbClient.client, dao).build
      } yield db.get
    }

  private def buildDAO(dbClient: DbClientEnv): UIO[DAO] =
    (
      (buildRefined(SettingsModels.toConcurrency(1)) &&&
        buildRefined(SettingsModels.toDatabase("$name$"))) >>= {
        case (concurrency, database) =>
          DatabaseService.build(dbClient.client, concurrency, database, Some("test_"))
      }
    ).mapError(throw _)

  private def buildRefined[A](either: Either[String, A]): Task[A] =
    ZIO.fromEither(either).mapError(new RuntimeException(_))
}
