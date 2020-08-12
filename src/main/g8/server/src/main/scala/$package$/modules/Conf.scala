package $package$.modules

import $package$.models.SettingsModels.Settings
import zio.Has
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer

object conf {
  type Conf = Has[Conf.Service]

  object Conf {
    trait Service {
      def settings: Settings
    }

    def live(_settings: Settings): ULayer[Has[Service]] = ZLayer.succeed(
      new Service {
        val settings: Settings = _settings
      }
    )
  }

  def settings[A](f: Settings => A = identity[Settings](_)): URIO[Conf, A] = 
    ZIO.access[Conf](conf => f(conf.get.settings))
}
