package $package$.services

import $package$.log.Logger._
import $package$.models.SettingsModels._
import eu.timepit.refined.auto._
import eu.timepit.refined.pureconfig._
import pureconfig.ConfigObjectSource
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.RIO
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.clock.Clock
import zio.duration._

object ConfigurationService {
  def load(
    namespace: String,
    extraConf: ConfigObjectSource
  ): RIO[Clock, Settings] = {
    for {
      settingsOrError <- Task(extraConf.withFallback(ConfigSource.default).at(namespace).load[Settings])
      _               <- log.info(s"Configuration loaded: \$settingsOrError")
      settings        <- ZIO.fromEither(settingsOrError).mapError(error => new RuntimeException(error.toString))
    } yield settings
  }.retry(schedule(extraConf))

  private def schedule(extraConf: ConfigObjectSource) = extraConf match {
    case ConfigSource.empty => Schedule.stop
    // From times to times extraConf is not yet loaded in the classpath (used in tests)
    // - scheduling a retry in this case
    // - happens only with sbt test, not bloop test
    case _ => Schedule.recurs(10) && Schedule.spaced(100.milliseconds)
  }
}
