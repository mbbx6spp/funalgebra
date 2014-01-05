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
  // when I used strings to lookup config attributes
  // in a Map I did this: +config.get("prot")+ and
  // spent 20 minutes wondering why it NPEs on looking
  // up the port.
  final case object DataSourceHost extends DataSourceAttribute
  final case object DataSourcePort extends DataSourceAttribute
  final case object DataSourceDriver extends DataSourceAttribute
  final case object DataSourceProtocol extends DataSourceAttribute
  final case object DataSourceName extends DataSourceAttribute
  // TODO uncomment final if/when scalac works as expected.
  // This outer reference cannot be check balogne is getting boring.
  /*final*/ case class DataSourceAttributeOther(name: String) extends DataSourceAttribute
  sealed trait DataSourceAttribute
  object DataSourceAttribute {
    def apply(s: String): DataSourceAttribute = s.toLowerCase match {
      case "host"     => DataSourceHost
      case "port"     => DataSourcePort
      case "driver"   => DataSourceDriver
      case "protocol" => DataSourceProtocol
      case "name"     => DataSourceName
      case other      => DataSourceAttributeOther(other)
    }
  }

  // Represents all DB protocols supported
  final case object MysqlProtocol extends DataSourceProtocolType
  final case object PostgresProtocol extends DataSourceProtocolType
  final case object SqliteProtocol extends DataSourceProtocolType
  sealed trait DataSourceProtocolType {
    override def toString: String =
      this.getClass
          .getSimpleName
          .reverse
          .substring(9)
          .reverse
          .toLowerCase
  }
  object DataSourceProtocolType {
    def apply(s: String): Option[DataSourceProtocolType] =
      s.toLowerCase match {
        case "mysql" => MysqlProtocol.some
        case "postgres" => PostgresProtocol.some
        case "postgresql" => PostgresProtocol.some
        case "sqlite" => SqliteProtocol.some
        case _ => none[DataSourceProtocolType]
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

  type DataSourceConfigMap = Map[DataSourceAttribute, String]
  object DataSourceConfigMap {
    def fromJson(j: String): Option[Map[DataSourceAttribute, String]] =
      for {
        m <- j.decodeOption[Map[String, String]]
      } yield fromMap(m)

    def fromMap(mm: Map[String, String]): Map[DataSourceAttribute, String] =
      mm.map { kv => DataSourceAttribute(kv._1) -> kv._2 }
  }

  type ReaderTOption[A, B] = ReaderT[Option, A, B]
  object ReaderTOption extends KleisliFunctions
                       with KleisliInstances {
    def apply[A, B](f: A => Option[B]): ReaderTOption[A, B] =
      kleisli(f)
  }

  type DataSourceReaderTOption[A] = ReaderTOption[DataSourceConfigMap, A]
  object DataSourceReaderTOption {
    def apply[A](f: DataSourceConfigMap => Option[A]): DataSourceReaderTOption[A] =
      ReaderTOption[DataSourceConfigMap, A](f)
  }
}

trait ConfigurationInstances extends ConfigurationTypes {
  import argonaut._, Argonaut._

  // Is this needed now?
  implicit def DataSourceConfigMapDecodeJson =
    DecodeJson(c => for {
      host      <- (c --\ "host").as[String]
      port      <- (c --\ "port").as[String]
      driver    <- (c --\ "driver").as[String]
      protocol  <- (c --\ "protocol").as[String]
      name      <- (c --\ "name").as[String]
    } yield Map(
      DataSourceHost -> host,
      DataSourcePort -> port,
      DataSourceDriver -> driver,
      DataSourceProtocol -> protocol,
      DataSourceName -> name))

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
    protocol: DataSourceProtocolType,
    name:     String) extends DataSource {

    def toUrl: String =
      protocol + "://" + host + ":" + port + "/" + name
  }

  def lookupString(key: DataSourceAttribute) =
    DataSourceReaderTOption[String] { _.get(key) }

  def lookupInteger(key: DataSourceAttribute) =
    DataSourceReaderTOption[Integer] { map =>
      // Gnarly stuff when interoping with Java, but at least
      // we make the interface of this function sensible.
      try {
        for { s <- map.get(key) } yield Integer.valueOf(s)
      } catch {
        case _: NumberFormatException => none[Integer]
      }
    }

  def lookupDataSourceProtocolType(key: DataSourceAttribute) =
    DataSourceReaderTOption[DataSourceProtocolType] { map =>
      DataSourceProtocolType(map.get(key) | "")
    }

  def getDataSource: DataSourceReaderTOption[DataSource] =
    // Note: Monadic style is not considered good form here
    // I am trying to avoid those "crazy" combinators that
    // everyone complains about in Scalaz just to work through
    // this example.
    for {
      h <- lookupString(DataSourceHost)
      p <- lookupInteger(DataSourcePort)
      d <- lookupString(DataSourceDriver)
      q <- lookupDataSourceProtocolType(DataSourceProtocol)
      n <- lookupString(DataSourceName)
    } yield DataSourceImpl(h, p, d, q, n)

  /* Below are all functions related to reading/loading/parsing/resolving
   * configuration sources/inputs to be able to provide the DataSourceConfigMap
   * to getDataSource to get the desired DataSource value out (if the
   * configuration is good, otherwise we get a None.
   */

  def parseConfig(bs: BufferedSource): DataSourceConfigMap =
    DataSourceConfigMap.fromJson(bs.mkString) | DataSourceConfigMap.fromMap(Map[String, String]())

  def fromSource(uri: JURL)(implicit codec: Codec): IO[BufferedSource] =
    IO { JSource.fromURL(uri)(codec) }

  def fromFixedStringSource: IO[BufferedSource] =
    IO {
      new BufferedSource(new ByteArrayInputStream("""
      {
        "host": "localhost",
        "port": "5432",
        "driver": "my.awesome.PostgresDriver",
        "protocol": "postgres",
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

  def loadConfig(env: Environment): IO[DataSourceConfigMap] =
    for {
      s <- sourceForEnvironment(env)
    } yield parseConfig(s)

  def configure(env: Environment): IO[Option[DataSource]] =
    for {
      cfg <- loadConfig(env)
    } yield getDataSource(cfg)
}

trait ConfigurationUsage extends ConfigurationFunctions {
  // Example test arguments for just the getDataSource definition above
  val goodConfig: DataSourceConfigMap = Map(
    DataSourceHost      -> "mydbhost",
    DataSourcePort      -> "3306",
    DataSourceProtocol  -> "mysql",
    DataSourceDriver    -> "my.awesome.MysqlDriver",
    DataSourceName      -> "contactsdb"
  )
  val badConfig: DataSourceConfigMap = Map(
    DataSourceHost -> "mydbhost",
    DataSourcePort -> "3306",
    DataSourceName -> "contactsdb"
  )

  // In Scala console try:
  def getDataSourceWithGoodConfig = getDataSource(goodConfig)
  def getDataSourceWithBadConfig  = getDataSource(badConfig)
}
