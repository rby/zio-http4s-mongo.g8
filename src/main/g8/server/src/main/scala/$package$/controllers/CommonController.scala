package $package$.controllers

import $package$.RoutesModels._
import $package$.controllers.CommonModels.Paginate
import io.circe.generic.JsonCodec
import org.mongodb.scala.FindObservable
import sttp.model.StatusCode.BadRequest
import sttp.model.StatusCode.Conflict
import sttp.model.StatusCode.InternalServerError
import sttp.model.StatusCode.NotFound
import sttp.tapir._
import sttp.tapir.json.circe._

trait CommonController {

  protected val errorOut: EndpointOutput[ErrorResponse] =
    oneOf(
      statusMapping(BadRequest, jsonBody[BadRequestError]),
      statusMapping(Conflict, jsonBody[AlreadyExistsError]),
      statusMapping(InternalServerError, jsonBody[ServerError]),
      statusMapping(NotFound, jsonBody[NotFoundError])
    )

  protected val paginate: EndpointInput[Paginate] =
    query[Option[Int]]("limit")
      .and(query[Option[Int]]("offset"))
      .mapTo(Paginate)
}

object CommonModels {

  @JsonCodec
  case class PaginationResponse(total: Long)

  sealed trait PaginateMode extends Product with Serializable

  case object NoPagination extends PaginateMode

  case class Paginate(
    limit: Option[Int],
    offset: Option[Int])
      extends PaginateMode

  private def limitOrDefault(paginate: Paginate)  = paginate.limit.getOrElse(10)
  private def offsetOrDefault(paginate: Paginate) = paginate.offset.getOrElse(0)

  implicit class PaginateSet[A](set: Set[A]) {
    def paginate(paginateMode: PaginateMode): Set[A] = paginateMode match {
      case NoPagination       => set
      case paginate: Paginate => set.drop(offsetOrDefault(paginate)).take(limitOrDefault(paginate))
    }
  }

  implicit class PaginateFindObservable[A](obs: FindObservable[A]) {
    def paginate(paginateMode: PaginateMode): FindObservable[A] = paginateMode match {
      case NoPagination       => obs
      case paginate: Paginate => obs.skip(offsetOrDefault(paginate)).limit(limitOrDefault(paginate))
    }
  }
}
