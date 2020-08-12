package $package$.controllers

import HealthCheckModels._
import $package$.Main
import $package$.RoutesModels._
import $package$.services.PingService
import $package$.tapir.ZioSupport._
import io.circe.generic.JsonCodec
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.json.circe._
import zio.Task

object HealthCheckController extends CommonController {

  val ping: Endpoint[Unit, ErrorResponse, PingResponse, Nothing] =
    endpoint.get
      .in("ping")
      .out(jsonBody[PingResponse])
      .errorOut(errorOut)
      .tag("healthcheck")

  def endpoints = List(ping)

  def route(env: Main.Env): HttpRoutes[Task] =
    ping.toZioRoutes(env)(_ => PingService.ping)
}

object HealthCheckModels {

  @JsonCodec
  case class PingResponse(
    result: String,
    mongo: Boolean)

}
