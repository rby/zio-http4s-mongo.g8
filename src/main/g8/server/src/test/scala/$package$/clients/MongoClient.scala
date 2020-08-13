package $package$.clients

import org.mongodb.scala.MongoClient
import zio.Has
import zio.Managed
import zio.Task
import zio.ULayer
import zio.ZLayer

object MongoDbClient {
  trait DbClientEnv  { val client: MongoClient }
  object DbClientEnv { def apply(_client: MongoClient) = new DbClientEnv { val client = _client } }

  def asLayer: ULayer[Has[DbClientEnv]] =
    ZLayer.fromManaged(
      Managed
        .fromAutoCloseable(Task(MongoClient("mongodb://localhost:12345")))
        .map(DbClientEnv(_))
        .mapError(throw _)
    )
}
