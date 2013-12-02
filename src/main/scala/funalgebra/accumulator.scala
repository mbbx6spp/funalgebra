package funalgebra.accumulator

trait Accumulator[A] {
  def append(x: A, y: A): A
  def identity: A
}

object Accumulator {
  def concat[A](s: Seq[A])(implicit a: Accumulator[A]): A =
    s.foldLeft(a.identity)( (x, y) => a.append(x, y) )
}

