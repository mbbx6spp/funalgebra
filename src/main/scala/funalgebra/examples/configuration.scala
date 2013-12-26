package funalgebra.examples

trait ConfigurationTypes {
  import scalaz._, Scalaz._
  import scalaz.syntax.std._
  import argonaut._, Argonaut._
  import scala.language.postfixOps

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
  final case object DbHost extends DbConfigAttr
  final case object DbPort extends DbConfigAttr
  final case object DbDriver extends DbConfigAttr
  final case object DbProtocol extends DbConfigAttr
  final case object DbName extends DbConfigAttr
  // TODO uncomment final if/when scalac works as expected.
  // This outer reference cannot be check balogne is getting boring.
  /*final*/ case class DbAttrOther(name: String) extends DbConfigAttr
  sealed trait DbConfigAttr
  object DbConfigAttr {
    def apply(s: String): DbConfigAttr = s.toLowerCase match {
      case "host"     => DbHost
      case "port"     => DbPort
      case "driver"   => DbDriver
      case "protocol" => DbProtocol
      case "name"     => DbName
      case other      => DbAttrOther(other)
    }
  }

  // Represents all DB protocols supported
  final case object MysqlProtocol extends DbProtocolType
  final case object PostgresProtocol extends DbProtocolType
  final case object SqliteProtocol extends DbProtocolType
  sealed trait DbProtocolType {
    override def toString: String =
      this.getClass
          .getSimpleName
          .reverse
          .substring(9)
          .reverse
          .toLowerCase
  }
  object DbProtocolType {
    def apply(s: String): Option[DbProtocolType] =
      s.toLowerCase match {
        case "mysql" => MysqlProtocol.some
        case "postgres" => PostgresProtocol.some
        case "postgresql" => PostgresProtocol.some
        case "sqlite" => SqliteProtocol.some
        case _ => none[DbProtocolType]
      }
  }

  // Represents the kind of environment the application is
  // being run in.
  sealed trait Environment {
    def name: String =
      this.getClass
          .getSimpleName
          .toLowerCase
  }
  final case object LocalDev extends Environment
  // allow for multiple Qa environments
  // TODO uncomment out final if/when scalac works as expected
  /*final*/ case class QA(number: Int) extends Environment {
    override def name: String = "QA" + number
  }
  final case object StableDev extends Environment
  final case object Staging extends Environment
  final case object Production extends Environment

  type DbConfigMap = Map[DbConfigAttr, String]
  object DbConfigMap {
    def fromJson(j: String): Option[Map[DbConfigAttr, String]] =
      for {
        m <- j.decodeOption[Map[String, String]]
      } yield fromMap(m)

    def fromMap(mm: Map[String, String]): Map[DbConfigAttr, String] =
      mm.map { kv => DbConfigAttr(kv._1) -> kv._2 }
  }

  type ReaderTOption[A, B] = ReaderT[Option, A, B]
  object ReaderTOption extends KleisliFunctions
                       with KleisliInstances {
    def apply[A, B](f: A => Option[B]): ReaderTOption[A, B] =
      kleisli(f)
  }

  type DbReaderTOption[A] = ReaderTOption[DbConfigMap, A]
  object DbReaderTOption {
    def apply[A](f: DbConfigMap => Option[A]): DbReaderTOption[A] =
      ReaderTOption[DbConfigMap, A](f)
  }
}

trait ConfigurationInstances {
  import argonaut._, Argonaut._

  //implicit def DbConfigMapCodecJson =

  // Needed implicit later on in resolving codec for reading/loading
  // configuration sources.
  implicit val codec = scala.io.Codec("UTF-8")
}

trait ConfigurationFunctions extends ConfigurationTypes
                             with ConfigurationInstances {
  import scala.io.{Codec, BufferedSource, Source => JSource}
  import java.io.{File => JFile}
  import java.net.{URL => JURL}
  import java.io.ByteArrayInputStream
  import scalaz._, Scalaz._, syntax.std._, effect._

  // DataSource is publicly visible
  trait DataSource {
    def toUrl: String // might want to make this URL type later?
  }

  // DataSourceImpl is private to this object and only accessible
  // when functions in this object namespace have created it "sanitized"
  private final case class DataSourceImpl(
    host:     String,
    port:     Integer,
    driver:   String,
    protocol: DbProtocolType,
    name:     String) extends DataSource {

    def toUrl: String =
      protocol + "://" + host + ":" + port + "/" + name
  }

  def lookupString(key: DbConfigAttr) =
    DbReaderTOption[String] { _.get(key) }

  def lookupInteger(key: DbConfigAttr) =
    DbReaderTOption[Integer] { map =>
      // Gnarly stuff when interoping with Java, but at least
      // we make the interface of this function sensible.
      try {
        for { s <- map.get(key) } yield Integer.valueOf(s)
      } catch {
        case _: NumberFormatException => none[Integer]
      }
    }

  def lookupDbProtocolType(key: DbConfigAttr) =
    DbReaderTOption[DbProtocolType] { map =>
      DbProtocolType(map.get(key) | "")
    }

  def getDataSource: DbReaderTOption[DataSource] =
    for {
      h <- lookupString(DbHost)
      p <- lookupInteger(DbPort)
      d <- lookupString(DbDriver)
      q <- lookupDbProtocolType(DbProtocol)
      n <- lookupString(DbName)
    } yield DataSourceImpl(h, p, d, q, n)

  /* Below are all functions related to reading/loading/parsing/resolving
   * configuration sources/inputs to be able to provide the DbConfigMap
   * to getDataSource to get the desired DataSource value out (if the
   * configuration is good, otherwise we get a None.
   */


  def readConfig(f: JSource): IO[String] =
    sys.error("todo(1)")

  def parseConfig(s: JSource): IO[DbConfigMap] =
    sys.error("todo(2)")

  def fromSource(uri: JURL)(implicit codec: Codec): IO[BufferedSource] =
    IO { JSource.fromURL(uri)(codec) }

  def fromFixedStringSource: IO[BufferedSource] =
    IO {
      new BufferedSource(new ByteArrayInputStream("""
      {
        "host": "localhost",
        "port": "3306",
        "protocol": "postgres",
        "driver": "my.awesome.PostgresDriver",
        "name": "contactsdb"
      }
      """.getBytes(codec.charSet)))
    }

  def getRestApiUri(env: Environment): JURL =
    new JURL("http://localhost:9000/")

  def getFileUri(env: Environment): JURL =
    (new JFile("etc/funalgebra." + env.name + ".conf")).toURI.toURL

  def sourceForEnvironment(env: Environment): IO[BufferedSource] =
    env match {
      case LocalDev   => fromFixedStringSource
      case Production => fromSource(getRestApiUri(env))
      case Staging    => fromSource(getRestApiUri(env))
      case _          => fromSource(getFileUri(env))
    }

  def loadConfig(env: Environment): IO[DbConfigMap] =
    for {
      s       <- sourceForEnvironment(env)
      parsed  <- parseConfig(s)
    } yield parsed
}

trait ConfigurationUsage extends ConfigurationFunctions {
  // Example test arguments for just the getDataSource definition above
  val goodConfig: DbConfigMap = Map(
    DbHost      -> "mydbhost",
    DbPort      -> "3306",
    DbProtocol  -> "mysql",
    DbDriver    -> "my.awesome.MysqlDriver",
    DbName      -> "contactsdb"
  )
  val badConfig: DbConfigMap = Map(
    DbHost -> "mydbhost",
    DbPort -> "3306",
    DbName -> "contactsdb"
  )

  // In Scala console try:
  def getDataSourceWithGoodConfig = getDataSource(goodConfig)
  def getDataSourceWithBadConfig  = getDataSource(badConfig)
}
