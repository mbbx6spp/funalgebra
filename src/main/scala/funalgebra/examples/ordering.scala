package funalgebra.examples

// Don't use this in your real code, this is only for demonstration
// purposes as scalaz has all this stuff with a couple of
// differences in implementation choices.

// This file demonstrates a port of the Ord type class from Haskell to
// Scala to show how you might do this for your own typeclasses, since
// there are many techniques of doing this in Scala, this just shows
// one way, which you might try to start off with until you get comfortable
// with the way the suits you best.

trait OrderingTypes {
  // First we start off with our "initial algebra" of algebraic data type
  // definitions of our problem space.
  sealed trait Ordering
  final case object LessThan extends Ordering
  final case object EqualTo extends Ordering
  final case object GreaterThan extends Ordering

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
      if (ord.compare(a1, a2) == LessThan) { a1 }
      else { a2 }

    def maximum[A](a1: A, a2: A)(implicit ord: Ord[A]): A =
      if (ord.compare(a1, a2) == GreaterThan) { a1 }
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
      (ord.compare(a1, a2) == LessThan)

    def isMaximum[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
      (ord.compare(a1, a2) == GreaterThan)

    def ===[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
      ord.compare(a1, a2) == EqualTo

    def =/=[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
      ord.compare(a1, a2) != EqualTo
  }
}

trait OrderingInstances extends OrderingTypes {
  implicit val IntOrd = new Ord[Int] {
    def compare(x: Int, y: Int): Ordering =
      if (x < y)      { LessThan }
      else if (x > y) { GreaterThan }
      else            { EqualTo }
  }

  implicit val DoubleOrd = new Ord[Double] {
    def compare(x: Double, y: Double): Ordering =
      if (x < y)      { LessThan }
      else if (x > y) { GreaterThan }
      else            { EqualTo }
  }
}

trait OrderingUsage extends OrderingTypes with OrderingInstances {
  val unorderedInts: List[Int] =
    5 :: 4 :: 7 :: 9 :: Nil

  val unorderedDoubles: List[Double] =
    6.3 :: 7.0 :: 3.2 :: 3.14159 :: 1.2 :: 95.4 :: Nil

  // Try:
  //scala> Ord.maximum(unorderedInts)
  //res1: Option[Int] = Some(9)

  //scala> Ord.minimum(unorderedInts)
  //res2: Option[Int] = Some(4)

  //scala> Ord.minimum(unorderedDoubles)
  //res3: Option[Double] = Some(1.2)

  //scala> Ord.maximum(unorderedDoubles)
  //res4: Option[Double] = Some(95.4)
}
