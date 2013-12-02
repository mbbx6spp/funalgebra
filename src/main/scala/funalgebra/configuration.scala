package funalgebra.configuration

import scalaz._, Scalaz._

// Represents all the database configuration
// attributes related to setting up DB connections
// in this application.
//
// Think of this as creating a more type safe way
// of extracting configuration. For some reason
// when used strings to lookup config attributes
// in a Map I did this: +config.get("prot") and
// spend 20 minutes wondering why it NPEs on looking
// up the PORT.
sealed trait DbConfigAttr
final case object DbHost extends DbConfigAttr
final case object DbPort extends DbConfigAttr
final case object DbDriver extends DbConfigAttr
final case object DbProtocol extends DbConfigAttr
final case object DbName extends DbConfigAttr

final case class DbConnection(
  host:     String,
  port:     String,
  driver:   String,
  protocol: String,
  name:     String) {

  def toJdbcUrl: String =
    protocol + "://" + host + ":" + port + "/" + name
}

object DbConfig {
  type DbConfigMap = Map[DbConfigAttr, String]
  type ReaderTOption[A, B] = ReaderT[Option, A, B]
  object ReaderTOption extends KleisliFunctions with KleisliInstances {
    def apply[A, B](f: A => Option[B]): ReaderTOption[A, B] = kleisli(f)
  }
  type DbReaderTOption = ReaderTOption[DbConfigMap, String]

  def lookup(key: DbConfigAttr) =
    ReaderTOption[DbConfigMap, String] { _.get(key) }

  def dbConnection =
    for {
      h <- lookup(DbHost)
      p <- lookup(DbPort)
      d <- lookup(DbDriver)
      q <- lookup(DbProtocol)
      n <- lookup(DbName)
    } yield DbConnection(h, p, d, q, n)
}
