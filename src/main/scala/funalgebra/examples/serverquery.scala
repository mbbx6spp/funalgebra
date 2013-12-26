package funalgebra.serverquery

/* Deploy domain modeling
 * We have a need to deploy our multi-tennant environment to numerous
 * "server" (which might be virtual), across many tiers, services, in
 * different environments.
 *
 * The purpose of this example is to show the following:
 * 1. How we model the different parts of this domain using algebraic data
 *    types.
 * 2. How we can separate the different concerns of deploying across many
 *    "server" at once from knowledge of the different tiers and services
 *    while insulating this from the notion of different environments.
 */

// This part is a sum type representing the notion of search clusters
// which might be used for different purposes of the multi-tenant product.
// We might need to identify the cluster so we can deploy in different ways
// or different code to them later.
sealed trait SearchCluster
final case object ClusterA extends SearchCluster
final case object ClusterB extends SearchCluster
final case object ClusterC extends SearchCluster

// This is another a sum type to describe the Role assigned to a server.
sealed trait Role
final case object WebApp extends Role
final case object RefService extends Role
final case object ReportingCollector extends Role
final case class  SearchIndexer(cluster: SearchCluster) extends Role

// Yet another sum type to represent the different environments
sealed trait Environment
final case object Production extends Environment
final case object Stage extends Environment
final case object Integration extends Environment
// This is because we might have more than one QA environment, which
// is a pared down environment, e.g. QA(1), QA(2), QA(5)
final case class  QA(id: Int) extends Environment
// Here might have many local development environments for each developer's
// laptop, e.g. Devel("mbbx6spp"), Devel("spotter")
// uid just identifies the username of the developer in this case, but it
// could be anything meaningful in this domain.
final case class  Devel(uid: String) extends Environment

// Essentially using this as a namespace for functions related to
// Environment values.
object Environment {
  // convenience functions for Environment values
  def isProduction: Environment => Boolean = _ match {
    case Production => true
    case _ => false
  }

  def domainFor: Environment => String = _ match {
    case Production   => "int.ourproduct.tld"
    case Stage        => "int.ourproductstage.tld"
    case Integration  => "int.ourproductinteg.tld"
    case QA(n)        => "int.ourproductqa" + n + ".tld"
    case Devel(u)     => "int.ourproductdev" + u + ".tld"
  }

  def fqHostnameMapper: Environment => Server => Server = e => s => s match {
    case Server(h, r) => Server(h + "." + domainFor(e), r)
  }

  // TODO: flesh this out more
}

// Here we need a container for result sets of queries
sealed trait Query[A, B]
// TODO define value construtors

// This is a product type
final case class Server(hostname: String, roles: Seq[Role])

object PlayArea extends App {
  /* Pattern matching
   *  val someRole: Role = WebApp
   *
   *  someRole match {
   *    case WebApp => println("WebApp")
   *    case Bastion => println("Bastion")
   *    case _ => println("Other")
   *  }
   *
   * somewhat useless for product types with one data constructor
   *  Server("spotter", Bastion :: Nil) match {
   *    case Server(hostname, roles) => println(hostname + " " + roles)
   *  }
   */

  type ServerList = Seq[Server]

  def serversFor(env: Environment): ServerList = env match {
    case Production => Seq(
      Server("web01",  WebApp :: Nil),
      Server("web02",  WebApp :: Nil),
      Server("ref01",  RefService :: Nil),
      Server("rep01",  ReportingCollector :: Nil),
      Server("inda01", SearchIndexer(ClusterA) :: Nil),
      Server("inda02", SearchIndexer(ClusterA) :: Nil),
      Server("inda03", SearchIndexer(ClusterA) :: Nil),
      Server("indb01", SearchIndexer(ClusterB) :: Nil),
      Server("indb02", SearchIndexer(ClusterB) :: Nil),
      Server("indb03", SearchIndexer(ClusterB) :: Nil),
      // You will see these aren't in other environments, perhaps
      // because hosting this search cluster is expensive :)
      Server("indc01", SearchIndexer(ClusterC) :: Nil),
      Server("indc02", SearchIndexer(ClusterC) :: Nil),
      Server("indc03", SearchIndexer(ClusterC) :: Nil)
    ) map Environment.fqHostnameMapper(env)
    case Stage => Seq(
      Server("web01",  WebApp :: Nil),
      Server("web02",  WebApp :: Nil),
      Server("ref01",  RefService :: Nil),
      Server("rep01",  ReportingCollector :: Nil),
      Server("inda01", SearchIndexer(ClusterA) :: Nil),
      Server("inda02", SearchIndexer(ClusterA) :: Nil),
      Server("indb01", SearchIndexer(ClusterB) :: Nil),
      Server("indb02", SearchIndexer(ClusterB) :: Nil),
      // hodge podge of a server definition which we could easily
      // have in a non-production environment
      Server("bla01",  WebApp :: RefService :: Nil)
    ) map Environment.fqHostnameMapper(env)
    case Integration => Seq(
      Server("integ01", WebApp :: RefService :: ReportingCollector :: Nil),
      Server("integ02", SearchIndexer(ClusterA) :: Nil),
      Server("integ03", SearchIndexer(ClusterB) :: Nil)
    ) map Environment.fqHostnameMapper(env)
    case QA(n) => Seq(
      Server("qa01", WebApp :: RefService :: ReportingCollector :: Nil)
    ) map Environment.fqHostnameMapper(env)
    case Devel(u) => Seq(
      Server(u + "01", WebApp :: RefService :: ReportingCollector :: Nil)
    ) map Environment.fqHostnameMapper(env)
  }

  // filtering by roles
  type Pred[A] = (A => Boolean)

  // This is also known as Monoid
  trait Accumulator[A] {
    def identity: A
    def append(x: A, y: A): A
  }

  // Again using this as a namespace for Accumulator related functions
  object Accumulator {
    def concat[A](l: List[A])(implicit a: Accumulator[A]): A =
      l.foldLeft(a.identity)(a.append)
  }

  // Define an Accumulator definition for the case of OR-ing predicates of
  // type Server => Boolean (aka Pred[Server]).
  val OR = new Accumulator[Pred[Server]] {
    def identity: Pred[Server] = _ => false
    def append(x: Pred[Server], y: Pred[Server]): Pred[Server] =
      s => x(s) || y(s)
  }

  // Define an Accumulator definition for the case of AND-ing predicates of
  // type Server => Boolean (aka Pred[Server]).
  val AND = new Accumulator[Pred[Server]] {
    def identity: Pred[Server] = _ => true
    def append(x: Pred[Server], y: Pred[Server]): Pred[Server] =
      s => x(s) && y(s)
  }

  // could be pulled in from a configuration file that changes more often than
  // deployment mechanics code itself, which is often desirable.
  def allClusters = ClusterA :: ClusterB :: ClusterC :: Nil
  def allIndexer = allClusters.map( SearchIndexer.apply(_) )

  def isRole(r: Role): Pred[Server] =
    s => s.roles.contains(r)

  def allWebApps(l: Seq[Server]): Seq[Server] =
    l.filter( isRole(WebApp) )

  def allRefServices(l: Seq[Server]): Seq[Server] =
    l.filter( isRole(RefService) )

  def allIndexers(l: Seq[Server]): Seq[Server] =
    l.filter(Accumulator.concat[Pred[Server]](allIndexer.map(isRole))(OR))

  println("WebApp")
  println(allWebApps(serversFor(Stage)))
  println

  println("Indexers")
  println(allIndexers(serversFor(Stage)))
  println

  println("WebApp+RefService")
  // Exercise: Generalize webAndRefServers into a function that takes a list
  // of roles and ANDs the predicates together. Then do one for OR-ing
  // predicates together.
  val webAndRefServers = Accumulator.concat[Pred[Server]]((WebApp :: RefService :: Nil).map( isRole ))(AND)
  println(serversFor(Stage).filter(webAndRefServers))
}
