package funalgebra.mapper

import scala.language.higherKinds

/** ONLY FOR DEMONSTRATION PURPOSES ONLY */

// Also known as a generic Functor.
// It's purpose is to map a "container" (I use this term very loosely)
// of a particular type into a container (of the same type of container)
// potentially with a different underlying value type.
// Examples of common "containers" that can be used as Functors are:
// List[A], Option[A], Either[A], ...
// Note: I use sealed here to restrict where these can be created.
// See companion object for "construction" functions.
sealed trait Mapper[F[_], A] {
  def fmap[B](f: A => B)(fa: F[A]): F[B]
  // TODO fix symbol name below to work in Scala
  //def <$[B](a: A)(fb: F[B]): F[A]
}

// Like elsewhere in the examples I am using this object as
// a namespace, but it is also a companion object for the Mapper[A, B]
// trait above too.
object Mapper {
  // TODO
}


