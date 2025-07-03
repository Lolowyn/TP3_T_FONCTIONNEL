ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version      := "0.1.0"

libraryDependencies ++= Seq(
  // on vire Vegas, on garde juste Scalatest/Scalactic
  "org.scalactic" %% "scalactic" % "3.2.16",
  "org.scalatest" %% "scalatest"  % "3.2.16" % Test
)
