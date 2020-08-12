import Dependencies._
import Dependencies.CompilerPlugins._
import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "$scala_version$"
ThisBuild / organization := "$package$"
ThisBuild / organizationName := "$organization$"

lazy val `$name$` = (project in file("."))
  .settings(publish := Unit)
  .aggregate(server)

lazy val server = project
  .enablePlugins(DockerPlugin, JavaServerAppPackaging, BuildInfoPlugin)
  .settings(compilerSettings)
  .settings(
    name := "$name$",
    sources in (Compile, doc) := Nil,
    libraryDependencies ++= Seq(
      `circe-parser`,
      `circe-refined`,
      enumeratum,
      `enum-circe`,
      `http4s-circe`,
      `http4s-server`,
      `mongo-driver`,
      `odin-core`,
      `odin-slf4j`,
      `odin-zio`,
      `pureconfig`,
      `refined`,
      `refined-pureconfig`,
      `sttp-core`,
      `sttp-zio`,
      `tapir-core`,
      `tapir-enum`,
      `tapir-http4s`,
      `tapir-json`,
      `tapir-openapi-docs`,
      `tapir-openapi-yaml`,
      `tapir-swagger-ui`,
      `zio-cats`,
      `zio-core`,
      `zio-streams`
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      `diff-json`,
      `embed-mongo`,
      `zio-test-sbt`,
      `zio-test`
    ).map(_ % Test),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Test / parallelExecution := false
  )
  .settings(
    scalacOptions += "-Ymacro-annotations" // for circe @JsonCodec
  )
  .settings(
    buildInfoKeys := Seq(name, version),
    buildInfoPackage := "build_metadata"
  )
  .settings(
      // other settings here
  )

lazy val compilerSettings = Def.settings(
  addCompilerPlugin(`better-monadic-for`),
  addCompilerPlugin(`kind-projector`)
)
