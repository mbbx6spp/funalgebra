module FunAlgebra.AlgTypes where

import Data.Monoid
import Data.Functor

-- {{PossiblyMaybe is a sum type. A value of type
-- PossiblyMaybe a can be constructed by one of
-- only two value constructors: Somefink a and
-- Nowt.
data PossiblyMaybe a = Somefink a
                     | Nowt deriving (Show, Eq, Ord)

instance Functor PossiblyMaybe where
  fmap g (Somefink x) = Somefink $ g x
  fmap g Nowt = Nowt

-- }}






















-- {{Pence newtype wraps penny value to resolve "correct"
-- monoid for our application uses. The "correct" monoid
-- will be application dependent.
newtype Pence = Pence { getPence :: Integer }
                deriving (Show, Ord, Eq)

instance Monoid Pence where
  mempty = Pence 0
  mappend (Pence x) (Pence y) = Pence (x + y)

-- One implementation of a Monoid of a PossiblyMaybe of as
-- Can you think of another implementation? What are the pros
-- and cons of each?
-- Also what is the problem of declaring this instance?
-- Can you think of a way to explicitly use one or the other
-- implementation when you want different behaviors?
instance Monoid a => Monoid (PossiblyMaybe a) where
  mempty = Somefink $ mempty
  mappend (Somefink x) (Somefink y) = Somefink $ mappend x y
  mappend (Somefink x) Nowt = Somefink $ mappend x mempty
  mappend Nowt (Somefink y) = Somefink $ mappend mempty y
  mappend Nowt Nowt = Nowt

-- Again this is like building with small solid building blocks
-- and just mixing the motar around their well defined interface

-- }}
