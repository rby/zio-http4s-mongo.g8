package $package$.mongodb

import $package$.log.Logger.log
import org.mongodb.scala.{ Observable, Observer }
import zio.Chunk
import zio.IO
import zio.ZIO
import zio.stream.Sink
import zio.stream.Stream

object ZioMongoExtensions {

  implicit class ObsConverter[T](val observable: Observable[T]) extends AnyVal {

    def toStream: Stream[Throwable, T] = toStream(identity)

    def toStream[E](handleError: Throwable => E): Stream[E, T] =
      Stream.effectAsync { f =>
        observable.subscribe {
          new Observer[T] {
            override def onNext(result: T): Unit = f(ZIO.succeed(Chunk(result)))

            override def onError(e: Throwable): Unit = f {
              log.error(s"onError: \${e.getMessage}", e).ignore *>
                ZIO.fail(Some(handleError(e)))
            }

            override def onComplete(): Unit = f(ZIO.fail(None))
          }
        }
      }
  }

  implicit class ZioConverter[A, +E](stream: Stream[E, A]) {
    private def collect[E1 >: E](notFoundError: => E1): IO[E1, A] =
      stream.all >>= { elements => ZIO.fromEither(elements.headOption.toRight(notFoundError)) }

    private def collectEmpty[E1 >: E](notEmptyError: => E1): IO[E1, Unit] =
      stream.all >>= { elements =>
        ZIO.fromEither {
          elements.headOption match {
            case None    => Right(())
            case Some(_) => Left(notEmptyError)
          }
        }
      }

    def head[E1 >: E](notFoundError: => E1): IO[E1, A] =
      collect(notFoundError)

    def empty[E1 >: E](notEmptyError: => E1): IO[E1, Unit] =
      collectEmpty(notEmptyError)

    def all: IO[E, List[A]] = stream.run(Sink.collectAll[A]).map(_.toList)

    def allToSet: IO[E, Set[A]] = stream.run(Sink.collectAllToSet[A])

    def headOption: IO[E, Option[A]] = stream.all.map(_.headOption)
  }

}
