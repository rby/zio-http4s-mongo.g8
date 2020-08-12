package $package$.tapir

import io.circe.DecodingFailure
import org.http4s.{ EntityBody, HttpRoutes }
import sttp.tapir.DecodeResult._
import sttp.tapir.Endpoint
import sttp.tapir.server.DecodeFailureContext
import sttp.tapir.server.ServerDefaults
import sttp.tapir.server.ServerDefaults.FailureMessages
import sttp.tapir.server.ServerDefaults.ValidationMessages
import sttp.tapir.server.http4s._
import zio.Task
import zio.ZIO
import zio.interop.catz._
import io.circe.ParsingFailure

object ZioSupport {

  private val decodeFailureHandler =
    ServerDefaults.decodeFailureHandler.copy(failureMessage = CustomFailureMessages.failureMessage)

  implicit val serverOptions: Http4sServerOptions[Task] =
    Http4sServerOptions.default[Task].copy(decodeFailureHandler = decodeFailureHandler)

  implicit class ZioEndpoint[R, I, E, O](e: Endpoint[I, E, O, EntityBody[Task]]) {
    def toZioRoutes(env: R)(logic: I => ZIO[R, E, O]): HttpRoutes[Task] =
      e.toRoutes(logic(_).provide(env).either)
  }
}

object CustomFailureMessages {
  def failureMessage(ctx: DecodeFailureContext): String = {

    val base = FailureMessages.failureSourceMessage(ctx.input)

    val detail = ctx.failure match {
      case InvalidValue(errors) if errors.nonEmpty =>
        ValidationMessages.validationErrorsMessage(errors)
      case Missing =>
        "no body provided"
      case Error(body, error: ParsingFailure) =>
        s"parse error: \${error.message} for \$body"
      case Error(body, error: DecodingFailure) =>
        s"parse error: \${error.message} (history: [\${error.history.mkString(", ")}]) for \$body"
      case Error(body, error) =>
        s"parse error: \$error for \$body"
      case err =>
        err.toString
    }

    s"\$base, \$detail"
  }
}
