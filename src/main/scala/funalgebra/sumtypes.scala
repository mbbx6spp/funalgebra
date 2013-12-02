package funalgebra.sumtypes

// Same as Haskell sum type example

sealed trait PossiblyMaybe[+A]
final case class Somefink[A](a: A) extends PossiblyMaybe[A]
final case object Nowt extends PossiblyMaybe[Nothing]

// ecommerce example

sealed trait OrderEvent
case class CancelOrder(cartId: Long) extends OrderEvent
case class CompleteOrder(id: Long) extends OrderEvent
case class RefundOrder(id: Long) extends OrderEvent

// RSVP site example

sealed abstract class UserRSVPState(userId: Long, eventId: Long)
case class RSVPUnanswered(userId: Long, eventId: Long)
  extends EventRSVPState(userId, eventId)
case class RSVPNo(userId: Long, eventId: Long, message: Option[String])
  extends EventRSVPState(userId, eventId)
case class RSVPYes(userId: Long, eventId: Long, guests: Int, message: Option[String])
  extends EventRSVPState(userId, eventId)


