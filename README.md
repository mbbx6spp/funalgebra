# Fun(ctional) Algebra By Example

I gave a talk at CodeMesh that I wanted to elaborate more in code and
this repository contains the examples codebase with some inline
documentation and commentary.

The external markdown based documentation in the README.md is still
a work in progress (WIP), so please bear with me.

## Prerequisites

I expect the reader to have a very basic understanding of what is valued in
functional programming, specifically:
* Valuing values and the honesty of the values returned. e.g. using `Option[A]`
  over `null` when Java interop-ing in Scala or using `Either[E, A]` or
  `E \/ A` over stack unwinding exception throwing, which leds to more total
  functions. And, of course, one kind of value can be a function. Even a
  function that accepts other functions as arguments and/or returns functions.
  How wild is that?
* Ability to read purpose from type signature or "shape" of functions. e.g.
  from the type `a -> a -> a` we can infer a choice is made from the
  first two `a` arguments as a logical pure function purpose. Can you think
  of anything else that the ultimate purpose could be without violating
  referential transparency?
* Valuing totality of functions, perhaps using custom types to express
  functions without resorting to defining partial functions. As mentioned
  above, throwing exceptions is a non-total way of doing things. A more
  functional way to encode an error has occurred is using types like
  `Either e a`. There are many generic and either specific functions that
  can be used to roll up, splice, and many other things collections of these
  `Either e a` values when necessary, not to mention highly useful combinators
  for such values to take appropriate error actions when necessary. It is just
  a new way to think about programming when used to using exceptions. And
  hopefully you will find it a more productive way to encode errors in your
  software as I have, if you haven't already.

The phrase functional programming in industrial usage encompasses a
broad range of *enforcement* for the above ideas at the language
level, so I am going to attempt to be embracing of many approaches
to "functional programming" as defined more loosely in industry
than is generally defined in academia.

The reader should also not be completely horrified by the idea of
thinking in more algebraic terms, using properties to gain a better
understanding of behaviour, inferring types, and their relationships
to one another.

Optional: I heavily encourage you to install both `haskell-platform` and
`scala` as most of the examples will be in these languages. I also have a
couple of Erlang examples so installing Erlang is also advised.

Note: Scala requires JDK 1.6 or above.

Recommended versions:
* Scala - 2.10
* GHC - 7.6
* Erlang - R16B03

## Recap: FP 101

First let's look at some common ideas in functional programming that
you may have seen in FP 101-style tutorials. Open GHCi up now and
follow along:

```ghci

import Control.Monad

let a1 = Just 43 -- Maybe Integer: result of a fully applied function

let a2 = Nothing :: Maybe Integer

let f1 = (2*)

liftM f1 a1
liftM f1 a2

```

Note: remember to double return after `let` statements in GHCi if you see
the prompt go from '>' to '|'.

The above example just shows us the ability for us to run functions on
underlying "wrapped" values effortlessly. Using functional techniques
we have more focus on the underlying, more "honest" values, than is
*popular* in OO style programming. This allows us to keep more focus
on the problem domain we are trying to solve rather than doing
gymnastics to workaround inflexible class hierarchies, large surface
area object interfaces, throwing (and on the consuming end, catching)
exceptions, which are popular programming styles in mainstream OO
langauges.

Promoting functions to first class citizens means we can think about
computational abstraction not just data abstraction. How is this? In a
more functional programming language we are *able* to abstract over
computation as we can apply functions passed in as arguments naturally
and construct functions as results when needed without extra fuss to
overcome the shortcomings of the language. Again keeping us closer to
the problem domain rather than working around language deficiencies.

Note: Java 8 may be offering lambdas as a new language feature, but
(a) it is not even released yet, (b) it still feels "tacked on" -
which it is, honestly - as opposed to a free flowing language construct
that feels natural to language natives (I used to be a huge fan of
Java at some point and spent almost a decade writing primarily Java
code). Much of the Java standard libraries need to be rewritten using
this for it to start to feel natural. Considering how long it took to
get lambdas in there (~15 years since first serious suggestion) I will
not hold my breath.

Let's review the type of the `liftM` function:

```ghci

Prelude> import Control.Monad
Prelude Control.Monad> :t liftM
liftM :: Monad m => (a1 -> r) -> m a1 -> m r

```

We can see that given `m` has an instance of a `Monad` defined for it, when
`liftM` is given a function from type `a1` to `r` and we give it a type of
`m a1`, `liftM` can give us the value of type `m r` which applies the first
argument to the *contained* value of `m a1` to yield the *wrapped* or
*contained* `m r` value.

In the example above type `a1` and `r` happen to be the same, but we could
have done something like the following:

```haskell

module FunAlgebra.Tutorial where

import Control.Monad (liftM)

data Currency = USD | EUR | GBP | CHF deriving (Show)

data Money = Money
  { moneyCurrency :: Currency
  , moneyAmount :: Integer } deriving (Show)

main :: IO ()
main = do
  let a1 = Just 43
  let a2 = Nothing :: Maybe Integer
  let rate = 1.31
  -- Let's just assume we know the currency is EUR because of the data feed
  -- we might be parsing at this time.
  let f1 = (Money EUR . (rate*) . fromInteger)
  putStrLn $ show $ liftM f1 a1
  putStrLn $ show $ liftM f1 a2

```

You can see that this might show the bones of a more useful example
though still very 101 textbook-y. Imagine the rate is retrieved
from a source that is updated often (or at least often enough for
our application's purposes). Then imagine that the values `a1` and
`a2` are retrieved either from a datastore or perhaps user input.

So you can pat yourself on the back, we **used** values of a type,
which has a Monad instance defined, i.e. we used a specific kind of
monad (`Maybe a`) in on specific way (`liftM`).

For those that aren't well acquainted with monads or those that
are obsessed with understanding how it is the key to the meaning
of life (I was there once too), here's the dirty little secret:

> The concept of a monad is really quite simple. It builds upon
> the concepts of functor and applicative, which as you will see
> later in this tutorial series are also simple. Definitionally
> each one (monad, applicative, functor) can be expressed in
> algebraic notation very precisely. Algebra is just our domain
> specific language, which happens to be perfect for defining
> abstract ideas very precisely and unambiguously.

The thing to remember here is that functor, applicative, monad
all have simple generic definitions and some `instance`s of these
monads will have very simple implementations as well, however,
some implementations will be more complex or complicated by
virtue of their purpose. We'll expand on this later in this
tutorial series.

So next up, we will look at starting out with an "initial algebra"
(in terms of closed algebraic data types) and building upon it
using a form of ad-hoc polymorphism to extend how these types
can be used across *more* generic functions without coupling
the initial definition of these types to the full array of
*interfaces* it may possess. We will do this, this time in Scala,
via one implementation of the typeclass pattern, which uses a
feature of Scala called `implicits`, which is a powerful and much
more general construct.

As with all powerful things, much harm may be caused when used
improperly. Beware of the sharp edges in Scala, there are many,
but remember it offers a lot of bang for your buck if you don't
always need adult supervision. However, it isn't for everyone! :)

## TODO: Laws & Order ;)

Ok that's a lame section title, but bare with me here...

TODO: Walk through bit by bit the Order typeclass porting from
Haskell to Scala without dependencies for coins (the old fashioned
physical coins used in US/UK/Euro countries not crypto-currencies).

* [Here is the fleshed out source](https://github.com/mbbx6spp/funalgebra/blob/master/src/main/scala/funalgebra/examples/ordering.scala)

## Further Examples

While I flesh out the guided text here are direct links to example code
demonstrating the utility of various ideas, structures, and types commonly
used in functional programming:

* Start out with describing your domain with closed algebraic data types: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/haskell/FunAlgebra/AlgTypes.hs
* Explore extending types with one manfiestation of the typeclass pattern in Scala: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/scala/funalgebra/examples/ordering.scala
* Stack monads together to defer evaluation/extraction of context until it is known: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/scala/funalgebra/examples/configuration.scala
* Abstracting over data and computation in the Erlang `either` module: https://github.com/mbbx6spp/chicagoerlang2013/blob/master/source/either.erl
* Testing code using algebraic properties and thinking: https://github.com/mbbx6spp/chicagoerlang2013/blob/master/source/algebraic_properties.erl

There are other examples sprinkled here too, but I am still working on some
of them.

Thanks for your patience!
