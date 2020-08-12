package $package$.services

import $package$.RoutesModels._
import $package$.controllers.HealthCheckModels._
import $package$.modules.database
import $package$.modules.database.Database
import $package$.mongodb.ZioMongoExtensions._
import zio.ZIO

object PingService {

  def ping: ZIO[Database, ErrorResponse, PingResponse] =
    for {
      mongoClient <- database.mongoClient
      databases   <- mongoClient.listDatabaseNames().toStream(MongoError.fromThrowable).all
    } yield {
      PingResponse("ok", databases.nonEmpty)
    }
}
