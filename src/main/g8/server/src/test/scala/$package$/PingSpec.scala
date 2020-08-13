package $package$

import $package$.servers.HttpMongoServer
import $package$.servers.HttpServer._
import sttp.model.StatusCode._
import zio.duration._
import zio.test.Assertion._
import zio.test.TestAspect.timeout
import zio.test._

object PingSpec extends DefaultRunnableSpec {
  val spec = suite("Ping routes:")(
    PingTests.pingTest
  ).provideCustomLayerShared(HttpMongoServer.asLayer) @@ timeout(10.seconds)
}

object PingTests {

  val pingTest = testM("ping should return ok") {
    for {
      resp <- get("/ping")
    } yield {
      assert(resp.code)(equalTo(Ok)) &&
      assert(resp.body)(isRight(equalTo("""{"result":"ok","mongo":true}""")))
    }
  }
}
