package $package$.extensions

import cats.data.NonEmptyList
import $package$.extensions.DocumentExtensions._
import java.util.regex.Pattern
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.BsonTransformer
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{ Filters, Sorts, Updates }
import scala.util.matching.Regex

object MongoModelExtensions {

  implicit class StringFilters(private val field: String) extends AnyVal {
    def ===[A](any: A)(implicit trs: BsonTransformer[A]): Bson = Filters.eq(field, trs(any))

    def !==[A](any: A)(implicit trs: BsonTransformer[A]): Bson = Filters.ne(field, trs(any))

    def ~==(value: String): Bson = Filters.regex(field, s"^\${Pattern.quote(value)}\$\$", "i")

    def exists(exists: Boolean): Bson = Filters.exists(field, exists)

    def in[A](elems: Iterable[A])(implicit trs: BsonTransformer[A]): Bson =
      Filters.in(field, elems.toSeq.map(trs(_)): _*)

    def in[A](elems: NonEmptyList[A])(implicit trs: BsonTransformer[A]): Bson =
      in(elems.toList)

    def nin[A](elems: Iterable[A])(implicit trs: BsonTransformer[A]): Bson =
      Filters.nin(field, elems.toSeq.map(trs(_)): _*)

    def all[A](elems: NonEmptyList[A])(implicit trs: BsonTransformer[A]): Bson =
      Filters.all(field, elems.toList.map(trs(_)): _*)

    def gt[A](any: A)(implicit trs: BsonTransformer[A]): Bson = Filters.gt(field, trs(any))

    def ~~~(r: Regex): Bson = Filters.regex(field, r)

    def isNonEmptyArray(): Bson = Filters.ne(field, Nil) && Filters.exists(field)
  }

  implicit class StringUpdates(private val field: String) extends AnyVal {
    def :=[A](any: A)(implicit trs: BsonTransformer[A]): Bson = Updates.set(field, trs(any))

    def setOnInsert(any: Any): Bson = Updates.setOnInsert(field, any)

    def push(any: Any): Bson = Updates.push(field, any)

    def addToSet(any: Any): Bson = Updates.addToSet(field, any)

    def addEachToSet[A](any: Iterable[A])(implicit trs: BsonTransformer[A]): Bson =
      Updates.addEachToSet(field, any.map(trs(_)).toList: _*)

    def pull(any: Any): Bson = Updates.pull(field, any)

    def pullAll[A](any: List[A])(implicit trs: BsonTransformer[A]): Bson =
      Updates.pullAll(field, any.map(trs(_)): _*)

    def unset: Bson =
      Updates.unset(field)

    def rename(newName: String): Bson = Updates.rename(field, newName)
  }

  implicit class BsonFilters(private val bson: Bson) extends AnyVal {
    def &&(other: Bson): Bson = Filters.and(bson, other)

    def &&(others: Iterable[Bson]): Bson = others.foldLeft(bson)(_ && _)

    def ||(other: Bson): Bson = Filters.or(bson, other)
  }

  implicit class StringSorts(private val field: String) extends AnyVal {
    def asc: Bson = Sorts.ascending(field)

    def desc: Bson = Sorts.descending(field)
  }

  def setOnInsert(any: Any)(implicit registry: CodecRegistry): Document =
    Document("\$setOnInsert" -> any.toDocument)
}
