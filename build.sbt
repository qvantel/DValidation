import sbt._

name := "dvalidation"

organization := "com.qvantel"

version := "0.4"

scalaVersion := "2.13.3"

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

crossScalaVersions := Seq("2.11.12", "2.12.12", "2.13.3")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

val scalazVersion = "7.3.2"

libraryDependencies += "org.scalaz" %% "scalaz-core" % scalazVersion % "provided"

libraryDependencies += "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := <url>https://github.com/qvantel/DValidation</url>
  <licenses>
    <license>
      <name>Apache 2 license</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:qvantel/DValidation.git</url>
    <connection>scm:git:git@github.com:qvantel/DValidation.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tobnee</id>
      <name>Tobias Neef</name>
      <url>http://atinu.net/</url>
    </developer>
  </developers>

