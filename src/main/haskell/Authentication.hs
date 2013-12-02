module FunAlgebra.Authentication where

data Password = ScryptPass { hashed :: String, salt :: String } deriving (Show)

data Credential = UserPass String Password
                | ApiToken String deriving (Show)

data UserType = FreeTierUser
              | SmbTierUser
              | EnterpriseTierUser deriving (Show)


