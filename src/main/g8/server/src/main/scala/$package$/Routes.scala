package $package$

import build_metadata.BuildInfo
import cats.data.{ NonEmptyList => NEL }
import cats.syntax.semigroupk._
import $package$.controllers.ExampleController
import $package$.controllers.HealthCheckController
import $package$.log.Logger.log
import $package$.models.SettingsModels.Settings
import $package$.modules.conf
import io.circe.Json
import io.circe.generic.JsonCodec
import java.time.DateTimeException
import org.http4s.HttpRoutes
import org.http4s.server.middleware.{ Logger => Http4sLogger }
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.Server
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio._
import zio.interop.catz._

object Routes {

  def httpRoutes: URIO[Main.Env, HttpRoutes[Task]] =
    for {
      env      <- ZIO.access[Main.Env](identity)
      settings <- conf.settings()
    } yield {
      (
        List(
          ExampleController.route(env),
        ).map(http4sLogger(_, settings)) ++
          List(
            HealthCheckController.route(env),
            swaggerUiRoutes(settings)
          )
      ).reduce(_ <+> _)
    }

  private def swaggerUiRoutes(settings: Settings) =
    new SwaggerHttp4s(
      List(
        ExampleController.endpoints,
      ).reduce(_ ++ _)
        .toOpenAPI(BuildInfo.name, BuildInfo.version)
        .servers(
          List(
            settings.env.value match {
              case "local" => Server("/", Some("Local development environment"))
              case env     => Server("/$name$", Some(s"\${env.toUpperCase} environment"))
            }
          )
        )
        .toYaml
    ).routes[Task]

  private def http4sLogger(
    httpRoutes: HttpRoutes[Task],
    settings: Settings
  ): HttpRoutes[Task] = {
    val logAction: String => Task[Unit] = log.info(_)
    Http4sLogger.httpRoutes(
      logHeaders = settings.logging.headers,
      logBody = settings.logging.body,
      logAction = Some(logAction)
    )(httpRoutes)
  }
}

object RoutesModels {

  sealed trait ErrorResponse extends Product with Serializable

  @JsonCodec
  sealed trait BadRequestError extends ErrorResponse

  case class GenericError(error: String)            extends BadRequestError
  case class UnhandledBodyError(body: Json)         extends BadRequestError
  case class InvalidStateError(states: NEL[String]) extends BadRequestError

  object InvalidStateError {
    def apply(
      state: String,
      states: String*
    ): InvalidStateError = InvalidStateError(NEL.of(state, states: _*))
  }

  @JsonCodec
  case class AlreadyExistsError(alreadyExists: String) extends ErrorResponse

  @JsonCodec
  case class NotFoundError(notFound: String) extends ErrorResponse

  @JsonCodec
  sealed trait ServerError extends ErrorResponse

  case class MongoError(error: String) extends ServerError

  object MongoError {
    def fromThrowable(e: Throwable): MongoError = MongoError(e.getMessage)
  }

  case class ClockError(error: String) extends ServerError

  object ClockError {
    def fromException(e: DateTimeException): ClockError = ClockError(e.getMessage)
  }
}
