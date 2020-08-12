package $package$.modules

import $package$.RoutesModels.ClockError
import java.time.DateTimeException
import java.time.Instant
import zio.ZIO
import zio.clock.Clock

object clock {

  def currentInstantAsException: ZIO[Clock, DateTimeException, Instant] =
    ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))

  def currentInstant: ZIO[Clock, ClockError, Instant] =
    currentInstantAsException.mapError(ClockError.fromException)
}
