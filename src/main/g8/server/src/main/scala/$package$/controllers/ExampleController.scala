package $package$.controllers

import $package$.Main
import $package$.tapir.ZioSupport._
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.json.circe._
import zio.Task
import zio.ZIO

object ExampleController extends CommonController {

  private val greet =
    endpoint.post
      .in("v1" / "greet")
      .in(jsonBody[String])
      .out(jsonBody[String])
      .errorOut(errorOut)
      .tag("greet")
      .summary("Greeting service")

  def endpoints = List(greet)

  def route(env: Main.Env): HttpRoutes[Task] =
    greet.toZioRoutes(env) { name =>
        ZIO.succeed(s"Hello, \$name")
    }

}

