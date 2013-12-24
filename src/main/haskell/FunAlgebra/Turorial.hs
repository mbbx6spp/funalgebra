module FunAlgebra.Tutorial where

import Control.Monad (liftM)

data Currency = USD | EUR | GBP | CHF deriving (Show)

data Money = Money
  { moneyCurrency :: Currency
  , moneyAmount :: Double } deriving (Show)

main :: IO ()
main = do
  let a1 = Just 43
  let a2 = Nothing :: Maybe Integer
  let rate = 1.31
  let f1 = (Money EUR . (rate*) . fromInteger)
  putStrLn $ show $ liftM f1 a1
  putStrLn $ show $ liftM f1 a2
