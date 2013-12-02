package funalgebra.balancesheet

// We only care about USD, GBP, EUR, and BTC in our fictional
// world.
sealed trait Currency
final case object USD extends Currency
final case object GBP extends Currency
final case object EUR extends Currency
final case object BTC extends Currency

//final case class Money[C <: Currency](amount: Long)
//
//// There are more types of assets, but this is for illustration
//// purposes. Don't focus on completeness of model for now.
//sealed abstract class Asset[C <: Currency](value: Money[C])
//final case class Cash[C <: Currency](value: Money[C]) extends Asset(value)
//final case class AccountsReceivable[C <: Currency](value: Money[C]) extends Asset(value)
//final case class FixedAsset[C <: Currency](value: Money[C]) extends Asset(value)
//final case class OtherAsset[C <: Currency](value: Money[C]) extends Asset(value)
//
//// There are many more kinds of liabilities, but this should be
//// good enough for example purposes.
//sealed abstract class Liability[C <: Currency](value: Money[C])
//final case class ShortTermBankDebt[C <: Currency](value: Money[C]) extends Liability(value)
//final case class AccountsPayable[C <: Currency](value: Money[C]) extends Liability(value)
//final case class DividendsPayable[C <: Currency](value: Money[C]) extends Liability(value)
//final case class WagesPayable[C <: Currency](value: Money[C]) extends Liability(value)
//final case class OtherLiability[C <: Currency](value: Money[C]) extends Liability(value)
//
//object BalanceSheet {
//  import codemesh2013.accumulator._
//
//  implicit object moneyAcc extends Accumulator[Money, C] {
//    def append(x: Money[C], y: Money[C]): Money[C] =
//      Money[C](x.amount + y.amount)
//    def identity: Money[C] = Money[C](0L)
//  }
//
//  // convenience functions
//  def bitcoins(amount: Long)  = Money[BTC.type](amount)
//  def dollars(amount: Long)   = Money[USD.type](amount)
//  def pounds(amount: Long)    = Money[GBP.type](amount)
//  def euros(amount: Long)     = Money[EUR.type](amount)
//
//  // fixture data
//  val dollarAssets: Seq[Asset[USD.type]] = Seq(
//    Cash(dollars(1000)),
//    AccountsReceivable(dollars(300)),
//    FixedAsset(dollars(10000)),
//    OtherAsset(dollars(5000))
//  )
//
//  val dollarLiabilities: Seq[Liability[USD.type]] = Seq(
//    ShortTermBankDebt(dollars(0)),
//    AccountsPayable(dollars(500)),
//    DividendsPayable(dollars(0)),
//    WagesPayable(dollars(100)),
//    OtherLiability(dollars(50))
//  )
//}
