package funalgebra.types

import scalaz._, Scalaz._
import scalaz.syntax.std._

object Configuration {
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

  type DbConfigMap = Map[DbConfigAttr, String]
  type ReaderTOption[A, B] = ReaderT[Option, A, B]
  object ReaderTOption extends KleisliFunctions with KleisliInstances {
    def apply[A, B](f: A => Option[B]): ReaderTOption[A, B] = kleisli(f)
  }
  type DbReaderTOption = ReaderTOption[DbConfigMap, String]
}
