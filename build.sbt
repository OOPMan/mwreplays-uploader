name := "mwreplays-uploader"

version := "0.1"

scalaVersion := "2.9.2"

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.9"

libraryDependencies += "net.databinder" %% "dispatch-mime" % "0.8.9"

libraryDependencies <+= scalaVersion { "org.scala-lang" % "scala-swing" % _ }