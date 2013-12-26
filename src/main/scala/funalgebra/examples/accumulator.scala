package funalgebra.accumulator

trait Accumulator[A] {
  def append(x: A, y: A): A
  def identity: A
}

// We call them accumulators here, but in abstract algebra these are called
// Monoids, which sounds like something from Dr. Who or possibly Star Trek.
object Accumulator {
  def concat[A](s: Seq[A])(implicit a: Accumulator[A]): A =
    s.foldLeft(a.identity)( (x, y) => a.append(x, y) )

  // "default" implicit accumulators over core types where there aren't
  // two accumulators of the type known
  implicit object StringAccumulator extends Accumulator[String] {
    def append(x: String, y: String): String = x + y
    def identity: String = ""
  }

}

// Examples of "accumulators"

// In more formal texts the addition accumulator over integers is also
// known as a surjective monoid.
// Since there are two known "accumulators" over the integers we
// shouldn't make one implicit over the other. Same with longs below.
object IntAddAccumulator extends Accumulator[Int] {
  def append(x: Int, y: Int): Int = x + y
  def identity: Int = 0
}

// In more formal texts the addition accumulator over integers is also
// known as a injective monoid.
object IntMultAccumulator extends Accumulator[Int] {
  def append(x: Int, y: Int): Int = x * y
  def identity: Int = 1
}

// In more formal texts the addition accumulator over longs is also
// known as a surjective monoid.
object LongAddAccumulator extends Accumulator[Long] {
  def append(x: Long, y: Long): Long = x + y
  def identity: Long = 0L
}

// In more formal texts the addition accumulator over longs is also
// known as a injective monoid.
object LongMultAccumulator extends Accumulator[Long] {
  def append(x: Long, y: Long): Long = x * y
  def identity: Long = 1L
}

