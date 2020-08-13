package $package$.servers

import HttpServer.HttpEnv
import MongoDbServer.DbEnv
import $package$.clients.MongoDbClient
import $package$.clients.MongoDbClient.DbClientEnv
import zio.Has
import zio.URLayer
import zio.ZEnv

object HttpMongoServer {

  def asLayer: URLayer[ZEnv, Has[HttpEnv] with Has[DbEnv] with Has[DbClientEnv]] =
    MongoDbServer.asLayer ++ MongoDbClient.asLayer ++ HttpServer.asLayer
}
