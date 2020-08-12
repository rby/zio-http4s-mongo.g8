package $package$.models

import eu.timepit.refined.api.RefType
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.collection.MinSize
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.Interval.OpenClosed
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.StartsWith
import eu.timepit.refined.string.{ IPv4, IPv6, Uri }

object SettingsModels {
  type Duration = Int Refined Positive
  type Env      = String Refined NonEmpty
  type Host     = String Refined (Equal["localhost"] Or IPv4 Or IPv6)
  type Port     = Int Refined Positive
  type URI      = String Refined Uri
  type Token    = String Refined (MinSize[50] And StartsWith["xoxb"])
  type Channel  = String Refined MinSize[8]

  type ValidDatabase = NonEmpty
  type Database      = String Refined ValidDatabase

  type ValidConcurrency = OpenClosed[0, 100]
  type Concurrency      = Int Refined ValidConcurrency

  val toConcurrency: Int => Either[String, Concurrency] =
    RefType[Refined].refine[ValidConcurrency](_)

  val toDatabase: String => Either[String, Database] =
    RefType[Refined].refine[ValidDatabase](_)

  case class Settings(
    http: HttpSettings,
    mongo: Mongo,
    logging: Logging,
    concurrentTasks: ConcurrentTasks,
    env: Env)

  case class HttpSettings(server: HttpServerSettings)

  case class HttpServerSettings(
    port: Port,
    host: Host,
    requestTimeoutInSeconds: Duration)

  case class Mongo(
    url: URI,
    database: Database,
    databasePrefix: Option[String],
    queryTimeoutInMillis: Duration)



  case class Logging(
    headers: Boolean,
    body: Boolean)


  case class ConcurrentTasks(
    createMongoIndexes: Concurrency,
    insertIntoMongo: Concurrency,
    fetchFromMongo: Concurrency)

}
