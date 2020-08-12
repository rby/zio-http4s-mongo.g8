package $package$

import cats.implicits._
import $package$.models.SettingsModels.Settings
import eu.timepit.refined.auto._
import org.http4s.HttpRoutes
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.TerminationType
import org.http4s.server.middleware.{ Metrics => Http4sMetrics }
import zio.Task
import zio.ZIO
import zio.interop.catz._
import zio.interop.catz.implicits._

case class Metrics(settings: Settings) extends MetricsOps[Task] {


  def apply(routes: HttpRoutes[Task]): HttpRoutes[Task] =
    Http4sMetrics(this, classifierF = classifierF)(routes)

  val classifierF: Request[Task] => Option[String] =
    _.uri.toString.some
      .filterNot(_ == "/ping")
      .filterNot(_.startsWith("/docs"))

  override def decreaseActiveRequests(classifier: Option[String]): Task[Unit] =
    ZIO.unit

  override def increaseActiveRequests(classifier: Option[String]): Task[Unit] =
    ZIO.unit

  override def recordAbnormalTermination(
    elapsed: Long,
    terminationType: TerminationType,
    classifier: Option[String]
  ): Task[Unit] =
    ZIO.unit

  override def recordHeadersTime(
    method: Method,
    elapsed: Long,
    classifier: Option[String]
  ): Task[Unit] =
    ZIO.unit

  override def recordTotalTime(
    method: Method,
    status: Status,
    elapsed: Long,
    classifier: Option[String]
  ): Task[Unit] =
    ZIO.unit

}
