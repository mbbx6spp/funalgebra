name := "funalgebra"

scalaVersion := "2.10.3"

val scalazVersion = "7.0.5"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-effect" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
  "org.scalaz" %% "scalaz-xml" % scalazVersion,
  "io.argonaut" %% "argonaut" % "6.0.1",
  "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"
)

scalacOptions += "-feature"

scalacOptions += "-unchecked"

initialCommands in console := "import scalaz._, Scalaz._"
