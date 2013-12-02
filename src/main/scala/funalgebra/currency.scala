package funalgebra.currency

import scalaz._, Scalaz._

trait Accumulator[A] {
  def append(x: A, y: => A): A
  def idenity: A
}

sealed trait Currency
final case object GBPCcy extends Currency
final case object USDCcy extends Currency
final case object CHFCcy extends Currency
final case object EURCcy extends Currency
final case object CNYCcy extends Currency

final case class Money[C <: Currency](amount: Long)

object Money {
  def apply[C <: Currency](ccy: C, amt: Long): Money[C] =
    Money[C](amt)
}

object CurrencyHelpers {
  def gbp(amount: Long) = Money(GBPCcy, amount)
  def usd(amount: Long) = Money(USDCcy, amount)
  def chf(amount: Long) = Money(CHFCcy, amount)
  def eur(amount: Long) = Money(EURCcy, amount)
  def cny(amount: Long) = Money(CNYCcy, amount)

  def GBP = gbp(_)
  def USD = usd(_)
  def CHF = chf(_)
  def EUR = eur(_)
  def CNY = cny(_)
}

