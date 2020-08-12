import sbt._

object Dependencies {
  private val diffJsonVersion   = "4.0.2"
  private val circeVersion      = "0.13.0"
  private val embedMongoVersion = "2.2.0"
  private val enumeratumVersion = "1.6.1"
  private val http4sVersion     = "0.21.4"
  private val mongoVersion      = "2.9.0"
  private val odinVersion       = "0.7.0"
  private val pureConfigVersion = "0.12.3"
  private val refinedVersion    = "0.9.14"
  private val sttpClientVersion = "2.1.5"
  private val tapirVersion      = "0.15.3"
  private val zioCatsVersion    = "2.1.3.0-RC15"
  private val zioVersion        = "1.0.0-RC20"

  // circe
  lazy val `circe-parser`  = "io.circe" %% "circe-parser"  % circeVersion
  lazy val `circe-refined` = "io.circe" %% "circe-refined" % circeVersion

  // diff-json
  lazy val `diff-json` = "org.gnieh" %% "diffson-circe" % diffJsonVersion

  // embed-mongo
  lazy val `embed-mongo` = "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % embedMongoVersion

  // enumeratum
  lazy val enumeratum   = "com.beachape" %% "enumeratum"       % enumeratumVersion
  lazy val `enum-circe` = "com.beachape" %% "enumeratum-circe" % enumeratumVersion

  // http4s
  lazy val `http4s-circe`  = "org.http4s" %% "http4s-circe"        % http4sVersion
  lazy val `http4s-server` = "org.http4s" %% "http4s-blaze-server" % http4sVersion

  // mongo
  lazy val `mongo-driver` = "org.mongodb.scala" %% "mongo-scala-driver" % mongoVersion

  // odin
  lazy val `odin-core`  = "com.github.valskalla" %% "odin-core"  % odinVersion
  lazy val `odin-slf4j` = "com.github.valskalla" %% "odin-slf4j" % odinVersion
  lazy val `odin-zio`   = "com.github.valskalla" %% "odin-zio"   % odinVersion

  // pureconfig
  lazy val `pureconfig` = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion


  // refined
  lazy val `refined`            = "eu.timepit" %% "refined"            % refinedVersion
  lazy val `refined-pureconfig` = "eu.timepit" %% "refined-pureconfig" % refinedVersion

  // sttp
  lazy val `sttp-core` = "com.softwaremill.sttp.client" %% "core"                          % sttpClientVersion
  lazy val `sttp-zio`  = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % sttpClientVersion

  // tapir
  lazy val `tapir-core`         = "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVersion
  lazy val `tapir-enum`         = "com.softwaremill.sttp.tapir" %% "tapir-enumeratum"         % tapirVersion
  lazy val `tapir-http4s`       = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % tapirVersion
  lazy val `tapir-json`         = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion
  lazy val `tapir-openapi-docs` = "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % tapirVersion
  lazy val `tapir-openapi-yaml` = "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion
  lazy val `tapir-swagger-ui`   = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % tapirVersion

  // zio
  lazy val `zio-cats`     = "dev.zio" %% "zio-interop-cats" % zioCatsVersion
  lazy val `zio-core`     = "dev.zio" %% "zio"              % zioVersion
  lazy val `zio-streams`  = "dev.zio" %% "zio-streams"      % zioVersion
  lazy val `zio-test-sbt` = "dev.zio" %% "zio-test-sbt"     % zioVersion
  lazy val `zio-test`     = "dev.zio" %% "zio-test"         % zioVersion

  object CompilerPlugins {
    private val kindProjectorVersion    = "0.10.3"
    private val betterMonadicForVersion = "0.3.1"

    lazy val `kind-projector`     = "org.typelevel" %% "kind-projector"     % kindProjectorVersion
    lazy val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % betterMonadicForVersion
  }
}
