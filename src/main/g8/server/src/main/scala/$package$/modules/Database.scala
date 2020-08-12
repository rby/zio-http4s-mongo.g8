package $package$.modules

import $package$.models.ExampleDAO
import $package$.modules.database.Database.DAO
import org.mongodb.scala.MongoClient
import zio.Has
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer

object database {
  type Database = Has[Database.Service]

  object Database {
    trait Service {
      def mongoClient: MongoClient
      def dao: DAO
    }

    case class DAO(exampleDAO: ExampleDAO)

    def live(
      _mongoClient: MongoClient,
      _dao: DAO
    ): ULayer[Has[Service]] = ZLayer.succeed(
      new Service {
        def mongoClient: MongoClient = _mongoClient
        def dao: DAO                 = _dao
      }
    )
  }

  def mongoClient: URIO[Database, MongoClient] =
    ZIO.access[Database](_.get.mongoClient)

  def dao[A](f: DAO => A): URIO[Database, A] =
    ZIO.access[Database](database => f(database.get.dao))
}
