package funalgebra.balancesheet

// We only care about USD, GBP, EUR, and BTC in our fictional
// world.
sealed trait Currency
sealed trait USD extends Currency
sealed trait GBP extends Currency
sealed trait EUR extends Currency
sealed trait BTC extends Currency

final case class BalanceSheet[C >: Currency](assets: List[Asset], liability: List[Liability])

final case class Money(amount: Long)

// There are more types of assets, but this is for illustration
// purposes. Don't focus on completeness of model for now.
sealed abstract class Asset(value: Money)
final case class Cash(value: Money) extends Asset(value)
final case class AccountsReceivable(value: Money) extends Asset(value)
final case class FixedAsset(value: Money) extends Asset(value)
final case class OtherAsset(value: Money) extends Asset(value)

// There are many more kinds of liabilities, but this should be
// good enough for example purposes.
sealed abstract class Liability(value: Money)
final case class ShortTermBankDebt(value: Money) extends Liability(value)
final case class AccountsPayable(value: Money) extends Liability(value)
final case class DividendsPayable(value: Money) extends Liability(value)
final case class WagesPayable(value: Money) extends Liability(value)
final case class OtherLiability(value: Money) extends Liability(value)

object BalanceSheet {
  import scalaz._, Scalaz._
  import funalgebra.accumulator._

  implicit object moneyAcc extends Accumulator[Money] {
    private val acc = LongAddAccumulator
    def append(x: Money, y: Money): Money = Money(x.amount + y.amount)
    def identity: Money = Money(0L)
  }

  // Exercise: you might be able to make a macro in 2.10 to create
  // Accumulators for all Asset types :)
  // Now repeat for Liability types
  implicit object cashAssetAcc extends Accumulator[Cash] {
    private val acc = implicitly[Accumulator[Money]]
    def append(x: Cash, y: Cash): Cash = Cash(acc.append(x.value, y.value))
    def identity: Cash = Cash(acc.identity)
  }

  // fixture data
  val cashAssets: Seq[Cash] = Seq(
    Cash(Money(403)),
    Cash(Money(557)),
    Cash(Money(13))
  )

  // Try:
  //Accumulator.concat(cashAssets)
}
