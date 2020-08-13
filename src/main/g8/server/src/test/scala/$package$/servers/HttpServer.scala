package $package$.servers

import $package$.Main
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.model.MediaType.ApplicationJson
import sttp.model.Uri
import zio._
import zio.clock.Clock
import zio.duration._

object HttpServer {
  type STTP        = SttpBackend[Task, Nothing, Nothing]
  type REQUEST     = Request[Either[String, String], Nothing]
  type RESPONSE[A] = Response[Either[String, A]]

  val host = "http://localhost:9000"

  trait HttpEnv  { val sttp: STTP }
  object HttpEnv { def apply(_sttp: STTP) = new HttpEnv { val sttp = _sttp } }

  def asLayer: URLayer[ZEnv, Has[HttpEnv]] = {
    val zioHttpEnv = for {
      _                     <- Main.run(List("mode=test")).fork >>= (_.disown)
      implicit0(sttp: STTP) <- AsyncHttpClientZioBackend()
      // we don't want the test clock for the retry schedule
      // using the live one
      clock <- ZIO.access[Clock](identity).provideLayer(Clock.live)
      _ <- basicRequest
            .get(uri"$host/ping")
            .response(asString)
            .send()
            .retry(Schedule.recurs(60) && Schedule.spaced(1.second).provide(clock))
    } yield HttpEnv(sttp)

    ZLayer.fromEffect(zioHttpEnv).mapError(throw _)
  }

  def get(
    path: String,
    f: REQUEST => REQUEST = identity
  ): ZIO[Has[HttpEnv], Throwable, RESPONSE[String]] =
    executeRequest(path, asString)(uri => f(basicRequest.get(uri)))

  def getBytes(
    path: String,
    f: REQUEST => REQUEST = identity
  ): ZIO[Has[HttpEnv], Throwable, RESPONSE[Array[Byte]]] =
    executeRequest(path, asByteArray)(uri => f(basicRequest.get(uri)))

  def post(
    path: String,
    body: String
  ): ZIO[Has[HttpEnv], Throwable, RESPONSE[String]] =
    executeRequest(path, asString)(basicRequest.post(_).body(body).contentType(ApplicationJson))

  def patch(
    path: String,
    body: String
  ): ZIO[Has[HttpEnv], Throwable, RESPONSE[String]] =
    executeRequest(path, asString)(basicRequest.patch(_).body(body).contentType(ApplicationJson))

  def put(
    path: String,
    body: String
  ): ZIO[Has[HttpEnv], Throwable, RESPONSE[String]] =
    executeRequest(path, asString)(basicRequest.put(_).body(body).contentType(ApplicationJson))

  private def executeRequest[A](
    path: String,
    responseAs: ResponseAs[Either[String, A], Nothing]
  )(request: Uri => REQUEST
  ): RIO[Has[HttpEnv], RESPONSE[A]] =
    for {
      implicit0(sttp: STTP) <- ZIO.access[Has[HttpEnv]](_.get.sttp)
      url                   = s"$host$path"
      resp                  <- request(uri"$url").response(responseAs).send()
    } yield resp
}
