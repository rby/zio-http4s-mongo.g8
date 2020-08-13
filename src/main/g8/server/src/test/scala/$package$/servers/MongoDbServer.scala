package $package$.servers

import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.IRuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import zio.Has
import zio.UIO
import zio.ULayer
import zio.ZLayer

object MongoDbServer {

  trait DbEnv  { val props: MongodProps }
  object DbEnv { def apply(_props: MongodProps) = new DbEnv { val props = _props } }

  case class MongodProps(
    mongodProcess: MongodProcess,
    mongodExe: MongodExecutable)

  def asLayer: ULayer[Has[DbEnv]] =
    ZLayer
      .fromAcquireRelease(mongoStart())(mongoStop)
      .map(p => Has(DbEnv(p.get)))

  private val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build()

  private val cmdOptions = new MongoCmdOptionsBuilder()
    .useStorageEngine("mmapv1")
    .build()

  private def mongoStart(
    port: Int = 12345,
    version: Version = Version.V3_6_5,
    runtimeConfig: IRuntimeConfig = runtimeConfig
  ): UIO[MongodProps] = UIO.effectTotal {
    val mongodExe: MongodExecutable = mongodExec(port, version, runtimeConfig)
    MongodProps(mongodExe.start(), mongodExe)
  }

  private def mongoStop(mongodProps: MongodProps): UIO[Unit] = UIO.effectTotal {
    mongodProps.mongodProcess.stop()
    mongodProps.mongodExe.stop()
  }

  private def mongodExec(
    port: Int,
    version: Version,
    runtimeConfig: IRuntimeConfig
  ): MongodExecutable =
    MongodStarter
      .getInstance(runtimeConfig)
      .prepare(
        new MongodConfigBuilder()
          .version(version)
          .cmdOptions(cmdOptions)
          .net(new Net(port, Network.localhostIsIPv6()))
          .build()
      )
}
