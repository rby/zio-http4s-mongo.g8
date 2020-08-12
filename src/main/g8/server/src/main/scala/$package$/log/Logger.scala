package $package$.log

import io.odin.formatter.Formatter
import io.odin.zio.consoleLogger
import io.odin.formatter.options.ThrowableFormat
import io.odin.formatter.options.PositionFormat

object Logger {

  private val isLocalDev =
    Option(System.getenv("jenkins")).isEmpty &&
      Option(System.getenv("GLOBAL_DEPLOYED_ENV")).isEmpty

  val formatter = Formatter.create(
    throwableFormat = ThrowableFormat.Default,
    positionFormat = if (!isLocalDev) PositionFormat.Full else PositionFormat.AbbreviatePackage,
    colorful = isLocalDev,
    printCtx = true
  )

  val log = consoleLogger(formatter)
}
