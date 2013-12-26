package funalgebra.possiblymaybe

// Imperative code you might write in Scala
// Hold your nose, it's foul and disgusting
class BadMysqlConnection(
   var  host: String,
        port: Int,
        name: String) {

  // If host is null set it to localhost
  if (host == null) {
    host = "localhost"
  }

  // Oh and best of all if name of DB is null let's just throw a
  // RuntimeException. What could taste more foul? Perfect!
  if (name == null) {
    throw new RuntimeException("name was null")
  }
}

/** BUT WAIT, LET'S DO A DO-OVER **/

// A sum type that represents a value that may or may not be set
// Yes, this is the Maybe type in Haskell or Option in Scala stdlib
sealed trait PossiblyMaybe[+A]
final case class Somefink[A](a: A) extends PossiblyMaybe[A]
final case object Nowt extends PossiblyMaybe[Nothing]

// Here are some potentially useful functions to use on PossiblyMaybe
// values.
object PossiblyMaybeOps {
  def noneDefault[A](pm: PossiblyMaybe[A])(a: A): A = pm match {
    case Somefink(x) => x
    // To cater to inputs that might have been retrieved via Java APIs
    // we catchall and return the default value, a.
    case _ => a
  }

  def noneFail[A](pm: PossiblyMaybe[A])(e: Error): Or[Error, A] = pm match {
    case Somefink(x) => Success[Error, A](x)
    case _ => Failure[Error, A](e)
  }
}

// Another sum type that represents either an error or a success value
// This is like the Either type in Haskell or Scala
sealed trait Or[+E, +A]
final case class Success[E, A](a: A) extends Or[E, A]
final case class Failure[E, A](e: E) extends Or[E, A]

// a product type (or record) to hold error message
final case class Error(message: String)

// Now how could we make this better?
class BetterMysqlConnection(private val maybeHost: PossiblyMaybe[String],
                            private val maybePort: PossiblyMaybe[Int],
                            private val maybeName: PossiblyMaybe[String]) {
  import PossiblyMaybeOps._

  // Here we can return a host string using default when necessary
  // Note: the maybeHost is a private val above.
  def host: String = noneDefault(maybeHost)("localhost")
  // Same for port here.
  def port: Int = noneDefault(maybePort)(3306)
  // Sometimes no values should be reported as errors
  def name: Or[Error, String] =
    noneFail(maybeName)(Error("No database name set"))
}

/** Now let's compare usage of both classes to compare **/

object PlayArea extends App {
  // Assume we use a Java library to pull hostnames from some configuration
  // and we might have some null values.
  val hostnames = List(null, "web01", "db01", "assets01", null, "ref01")

  // Now let's try to convert into Option[String]
  val maybeHostnames = hostnames map { Option(_) }

  // What happens when you do this in a console?
  //val badConn = new BadMysqlConnection(null, 3306, null)

  // What about this instead?

}
