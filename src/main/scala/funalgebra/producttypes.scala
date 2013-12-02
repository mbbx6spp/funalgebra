package funalgebra.producttypes

// Deploy domain

// This part is actually a sum type
sealed trait SearchCluster
final case object ClusterA extends SearchCluster
final case object ClusterB extends SearchCluster
final case object ClusterC extends SearchCluster

// So is this (a sum type)
sealed trait Role
final case object WebApp extends Role
final case object RefService extends Role
final case object Bastion extends Role
final case class  SearchIndexer(cluster: SearchCluster) extends Role

// This is a product type
final case class Server(hostname: String, roles: Seq[Role])

object PlayArea extends App {
  // somewhat useless for product types with one data constructor
//  Server("spotter", Bastion :: Nil) match {
//    case Server(hostname, roles) => println(hostname + " " + roles)
//  }
//
//  // Pattern matching
//  val someRole: Role = WebApp
//
//  someRole match {
//    case WebApp => println("WebApp")
//    case Bastion => println("Bastion")
//    case _ => println("Other")
//  }

  val stagingServers = Seq(
    Server("web01", WebApp :: Nil),
    Server("web02", WebApp :: Nil),
    Server("ref01", RefService :: Nil),
    Server("inda01", SearchIndexer(ClusterA) :: Nil),
    Server("inda02", SearchIndexer(ClusterA) :: Nil),
    Server("inda03", SearchIndexer(ClusterA) :: Nil),
    Server("indb01", SearchIndexer(ClusterB) :: Nil),
    Server("indb02", SearchIndexer(ClusterB) :: Nil),
    Server("indb03", SearchIndexer(ClusterB) :: Nil),
    Server("bla01", WebApp :: RefService :: Nil)
  )

  // filtering by roles
  type Pred[A] = (A => Boolean)

  trait Accumulator[A] {
    def identity: A
    def append(x: A, y: A): A
  }

  object Accumulator {
    def concat[A](l: List[A])(implicit a: Accumulator[A]): A =
      l.foldLeft(a.identity)(a.append)
  }

  object OrPredServerAccumulator extends Accumulator[Pred[Server]] {
    def identity: Pred[Server] = _ => false
    def append(x: Pred[Server], y: Pred[Server]): Pred[Server] = s => x(s) || y(s)
  }

  object AndPredServerAccumulator extends Accumulator[Pred[Server]] {
    def identity: Pred[Server] = _ => true
    def append(x: Pred[Server], y: Pred[Server]): Pred[Server] = s => x(s) && y(s)
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
    l.filter(Accumulator.concat[Pred[Server]](allIndexer.map(isRole))(OrPredServerAccumulator))

  println("WebApp")
  println(allWebApps(stagingServers))
  println

  println("Indexers")
  println(allIndexers(stagingServers))
  println

  println("WebApp+RefService")
  // TODO
  //println(stagingServers.filter(Accumulator.concat[Server]((WebApp :: RefService :: Nil).map( isRole ))(AndPredServerAccumulator)))

}
