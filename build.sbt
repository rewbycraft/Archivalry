import com.typesafe.sbt.SbtNativePackager._

import NativePackagerKeys._

import play.PlayImport.PlayKeys

lazy val root = (project in file(".")).enablePlugins(PlayScala)

maintainer in Linux := "Roelf Wichertjes <roelf@roelf.org>"

packageSummary in Linux := "Archivalry is a simple repository manager."

packageDescription := "Archivalry is simple repository manager. It doesn't do much but is very light."

rpmRelease := "1"

rpmVendor := "roelf.org"

rpmUrl := Some("http://github.com/rewbycraft/Archivalry")

rpmLicense := Some("MIT")

name := "Archivalry"

version := "0.1-SNAPSHOT"

organization := "org.roelf.scala.archivalry"

scalaVersion := "2.11.3"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "0.8.0"

resolvers += "RoundEights" at "http://maven.spikemark.net/roundeights"

libraryDependencies += "com.roundeights" %% "hasher" % "1.0.0"

libraryDependencies += "com.h2database" % "h2" % "1.4.182"
