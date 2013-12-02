package funalgebra.sumtypes

// Imperative code you might write in Scala
// Hold your nose, it's foul and disgusting
class BadMysqlConnection(var host: String,
                            port: Int,
                            name: String) {
  if (host == null) {
    host = "localhost"
  }

  if (name == null) {
    throw new RuntimeException("name was null")
  }

}



/** BUT WAIT, LET'S DO A DO-OVER **/

// Same as Haskell sum type example
sealed trait PossiblyMaybe[+A]
final case class Somefink[A](a: A) extends PossiblyMaybe[A]
final case object Nowt extends PossiblyMaybe[Nothing]

object PossiblyMaybeOps {
  def noneDefault[A](pm: PossiblyMaybe[A])(a: A): A = pm match {
    case Somefink(x) => x
    case _ => a
  }
}

// Now how could we make this better?
class BetterMysqlConnection(maybeHost: PossiblyMaybe[String],
                            port: Int = 3306,
                            maybeName: PossiblyMaybe[String]) {
  import PossiblyMaybeOps._

  def host: String = noneDefault(maybeHost)("localhost")
}
