# Fun(ctional) Algebra By Example

I gave a talk at CodeMesh that I wanted to elaborate more in code and this is
the examples codebase that emerged.

I plan to document the codebase in a serial story telling way in Markdown
documents linked to in this README, but I have already started inline
documenting the code with some explanations.

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

The phrase functional programming encompasses a broad range of *enforcement*
for the above ideas, so I am going to attempt to be embracing of many common
approaches to "functional programming".

The reader should also not be completely horrified by the idea of thinking in
more algebraic terms, using properties to gain a better understanding of
behaviour, inferring types, and their relationships to one another.

Optional: I heavily encourage you to install both `haskell-platform` and
`scala` as most of the examples will be in these languages. I also have a
couple of Erlang examples so installing Erlang is also advised.

Note: Scala requires JDK 1.6 or above.

Recommended versions:
* Scala - 2.10
* GHC - 7.6
* Erlang - R16B03

## Recap: FP 101

First let's look at some common ideas in functional programming that you
have probably seen in FP 101-style tutorials. Open GHCi up now and follow
along:

```ghci

import Control.Monad

let a1 = Just 43 -- Maybe Integer: result of a fully applied function

let a2 = Nothing :: Maybe Integer

let f1 = show . (2*)

liftM f1 a1
liftM f1 a2

```

Note: remember to double return after `let` statements in GHCi if you see
the prompt go from '>' to '|'.

The above example just shows us the ability for us to run functions on
underlying "wrapped" values effortlessly. Using functional techniques
we have more focus on the underlying, more "honest" values, than is
popular in OO style programming. Allowing us to keep more focus on the
problem domain we are trying to solve rather than doing gymnastics to
workaround the inflexible class hierarchies or large surface area object
interfaces.

Promoting functions to first class citizens means we can thinking about
computational abstraction not just data abstraction. How is this? In a
more functional programming language we are *able* to abstract over
computation as we can apply functions passed in as arguments naturally
and construct functions as results when needed without extra fuss. Again
keeping us closer to the problem domain rather than working around language
deficiencies.

Let's review the type of the `liftM` function:

```haskell

-- TODO: sorry out of time tonight

```

## Further Examples

While I flesh out the guided text here are direct links to example code
demonstrating the utility of various ideas, structures, and types commonly
used in functional programming:

* Start out with describing your domain with closed algebraic data types: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/haskell/AlgTypes.hs
* Explore extending types with one manfiestation of the typeclass pattern in Scala: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/scala/funalgebra/ordering.scala
* Stack monads together to defer evaluation/extraction of context until it is known: https://github.com/mbbx6spp/funalgebra/blob/master/src/main/scala/funalgebra/configuration.scala
* Abstracting over data and computation in the Erlang `either` module: https://github.com/mbbx6spp/chicagoerlang2013/blob/master/source/either.erl
* Testing code using algebraic properties and thinking: https://github.com/mbbx6spp/chicagoerlang2013/blob/master/source/algebraic_properties.erl

There are other examples sprinkled here too, but I am still working on some
of them.

Thanks for your patience!
