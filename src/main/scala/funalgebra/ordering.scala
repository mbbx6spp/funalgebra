package funalgebra.ordering

// This file demonstrates a port of the Ord type class from Haskell to
// Scala to show how you might do this for your own typeclasses, since
// there are many techniques of doing this in Scala, this just shows
// one way, which you might try to start off with until you get comfortable
// with the way the suits you best.

// First we start off with our "initial algebra" of algebraic data type
// definitions of our problem space.
sealed trait Ordering
final case object LT extends Ordering
final case object EQ extends Ordering
final case object GT extends Ordering

// Now this is the trait that defines our "typeclass" in Scala.
// This is a slight modification from what we see in Haskell since
// we only need to implement one trait method for this type class and the
// rest of the definitions of the functions from the Haskell typeclass
// come for free, I decided to separate them.
trait Ord[A] {
  def compare(a1: A, a2: A): Ordering
}

// Here are all the functions we may want to import into the local
// scope where we would want to use them.
object Ord {
  def minimum[A](a1: A, a2: A)(implicit ord: Ord[A]): A =
    if (ord.compare(a1, a2) == LT) { a1 }
    else { a2 }

  def maximum[A](a1: A, a2: A)(implicit ord: Ord[A]): A =
    if (ord.compare(a1, a2) == GT) { a1 }
    else { a2 }

  def minimum[A](xs: Seq[A])(implicit ord: Ord[A]): Option[A] = xs match {
    case x :: rest =>
      Some(rest.foldLeft(x) { (a: A, b: A) =>
        if (minimum(a, b)(ord) == a) { a } else { b } })
    case Nil => None
  }

  def maximum[A](xs: Seq[A])(implicit ord: Ord[A]): Option[A] = xs match {
    case x :: rest =>
      Some(rest.foldLeft(x) { (a: A, b: A) =>
        if (maximum(a, b)(ord) == a) { a } else { b } })
    case Nil => None
  }

  def isMinimum[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
    (ord.compare(a1, a2) == LT)

  def isMaximum[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
    (ord.compare(a1, a2) == GT)

  def ===[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
    ord.compare(a1, a2) == EQ

  def =/=[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
    ord.compare(a1, a2) != EQ
}

object IntOrd extends Ord[Int] {
  def compare(x: Int, y: Int): Ordering =
    if (x < y)      { LT }
    else if (x > y) { GT }
    else            { EQ }
}

object DoubleOrd extends Ord[Double] {
  def compare(x: Double, y: Double): Ordering =
    if (x < y)      { LT }
    else if (x > y) { GT }
    else            { EQ }
}

