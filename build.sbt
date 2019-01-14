import sbt._

name := "dvalidation"

organization := "net.atinu"

version := "0.3"

scalaVersion := "2.12.8"

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

crossScalaVersions := Seq("2.11.12", "2.12.8")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

val scalazVersion = "7.2.21"

libraryDependencies += "org.scalaz" %% "scalaz-core" % scalazVersion % "provided"

libraryDependencies += "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

//publishMavenStyle := true

//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}

publishTo := {
  val artifactory = "https://artifactory.qvantel.net/artifactory/"
  if (isSnapshot.value)
    Some("snapshots" at artifactory + "libs-snapshot-local")
  else
    Some("releases" at artifactory + "libs-release-local")
}

pomExtra := <url>https://github.com/tobnee/DValidation</url>
  <licenses>
    <license>
      <name>Apache 2 license</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:tobnee/DValidation.git</url>
    <connection>scm:git:git@github.com:tobnee/DValidation.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tobnee</id>
      <name>Tobias Neef</name>
      <url>http://atinu.net/</url>
    </developer>
  </developers>

