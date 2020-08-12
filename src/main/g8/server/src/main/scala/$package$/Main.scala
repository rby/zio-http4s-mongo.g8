package $package$

import $package$.models.SettingsModels._
import $package$.modules.conf
import $package$.modules.conf.Conf
import $package$.modules.database.Database
import $package$.services.ConfigurationService
import $package$.services.DatabaseService
import eu.timepit.refined.auto._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.mongodb.scala.MongoClient
import pureconfig.ConfigObjectSource
import pureconfig.ConfigSource
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import zio._
import zio.clock.Clock
import zio.console.{ putStrLn, Console }
import zio.interop.catz._
import zio.interop.catz.implicits._

object Main extends App {

  type Env = Conf with Database with ZEnv

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    ZIO.access[Clock](identity) >>= (
      program(parseArgs(args), _).foldM(
        err =>
          putStrLn(s"Execution failed with: \$err") *>
            ZIO.succeed(ExitCode.failure),
        ok =>
          putStrLn(s"Execution ok with: \$ok") *>
            ZIO.succeed(ExitCode.success)
      )
    )

  private def parseArgs(args: List[String]): ConfigObjectSource =
    if (args.contains("mode=test")) ConfigSource.resources("application-test.conf")
    else ConfigSource.empty

  private def program(
    extraConf: ConfigObjectSource,
    clock: Clock
  ): RIO[Console, Unit] =
    buildLayer(extraConf, clock).use(server.provideLayer(_))

  private def buildLayer(
    extraConf: ConfigObjectSource,
    clock: Clock
  ): TaskManaged[ULayer[Env]] =
    for {
      settings           <- Managed.fromEffect(ConfigurationService.load("$name$", extraConf).provide(clock))
      mongoClient        <- Managed.fromAutoCloseable(Task(MongoClient(settings.mongo.url)))
      createMongoIndexes = settings.concurrentTasks.createMongoIndexes
      database           = settings.mongo.database
      databasePrefix     = settings.mongo.databasePrefix
      dao                <- Managed.fromEffect(DatabaseService.build(mongoClient, createMongoIndexes, database, databasePrefix))
    } yield {
      Conf.live(settings) ++ Database.live(mongoClient, dao) ++ ZEnv.live
    }

  private def server: RIO[Env, Unit] =
    for {
      implicit0(r: Runtime[Env]) <- ZIO.runtime[Env]
      settings                   <- conf.settings()
      httpApp                    <- Routes.httpRoutes.map(Metrics(settings)(_))

      HttpServerSettings(port, host, timeout) = settings.http.server
      requestTimeout = timeout.value.seconds
      server <- BlazeServerBuilder[Task](ExecutionContext.global)
                 .bindHttp(port, host)
                 .withHttpApp(httpApp.orNotFound)
                 .withNio2(true)
                 .withWebSockets(false)
                 .withIdleTimeout(requestTimeout)
                 .withResponseHeaderTimeout(requestTimeout)
                 .serve
                 .compile
                 .drain
    } yield server
}
