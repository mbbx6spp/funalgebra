package funalgebra.configuration

import scalaz._, Scalaz._
import scalaz.syntax.std._

object DbConfig {
  import funalgebra.types.Configuration._

  // DbConnection is publically visible
  trait DbConnection {
    def toJdbcUrl: String
  }

  // DbConnectionImpl is private to this object and only accessible
  // when functions in this object namespace have created it "sanitized"
  private final case class DbConnectionImpl(
    host:     String,
    port:     String,
    driver:   String,
    protocol: String,
    name:     String) extends DbConnection {

    def toJdbcUrl: String =
      protocol + "://" + host + ":" + port + "/" + name
  }

  def lookup(key: DbConfigAttr) =
    ReaderTOption[DbConfigMap, String] { _.get(key) }

  def dbConnection: Kleisli[Option, DbConfigMap, DbConnection] =
    for {
      h <- lookup(DbHost)
      p <- lookup(DbPort)
      d <- lookup(DbDriver)
      q <- lookup(DbProtocol)
      n <- lookup(DbName)
    } yield DbConnectionImpl(h, p, d, q, n)

  // Example Usage below

  val goodConfig: DbConfigMap = Map(
    DbHost -> "mydbhost",
    DbPort -> "3306",
    DbProtocol -> "mysql",
    DbDriver -> "my.awesome.MysqlDriver",
    DbName -> "contactsdb"
  )

  val badConfig: DbConfigMap = Map(
    DbHost -> "mydbhost",
    DbPort -> "3306",
    DbName -> "contactsdb"
  )


  // In Scala console try:
  // dbConnection(goodConfig)
  // dbConnection(badConfig)
}
