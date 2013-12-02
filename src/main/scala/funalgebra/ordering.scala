package funalgebra.ordering

sealed trait Ordering
final case object LT extends Ordering
final case object EQ extends Ordering
final case object GT extends Ordering

trait Ord[A] {
  def compare(a1: A, a2: A): Ordering
}

object Ord {
  def minimum[A](a1: A, a2: A)(implicit ord: Ord[A]): A =
    if (ord.compare(a1, a2) == LT) { a1 }
    else { a2 }

  def maximum[A](a1: A, a2: A)(implicit ord: Ord[A]): A =
    if (ord.compare(a1, a2) == GT) { a1 }
    else { a2 }

  def ===[A](a1: A, a2: A)(implicit ord: Ord[A]): Boolean =
    ord.compare(a1, a2) == EQ
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
