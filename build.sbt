
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val myRealease = Command.command("publish-release") { state =>
  val stateWithStrictScalacSettings = Project.extract(state)
  val newState = stateWithStrictScalacSettings
    .appendWithSession(Seq(version := {
      val eVersion: String = stateWithStrictScalacSettings.getOpt(version).get
      val releaseVersion: String = eVersion.replace("-SNAPSHOT", "")
      println(s"v ${releaseVersion}")
      releaseVersion
    }), state)

  val (s, _) = Project.extract(newState).runTask((Compile / publish), newState)
  s
}
commands ++= Seq(myRealease)

//resolvers ++= Seq(
//  Resolver.mavenLocal,
//  Resolver.DefaultMavenRepository,
//  Resolver.sbtPluginRepo("releases"),
//)

ThisBuild / scalaVersion := "2.12.10"

val ivyLocal = Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

lazy val root = (project in file("."))
  .settings(
    name := "spark-noise-datasource",
    organization := "ru.beeline.dmp",
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "ru.beeline.dmp"
  )




val sparkVersion = "3.0.1"

val log4jVersion = "2.8.1"

libraryDependencies ++= Seq(

  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % Provided,

  "com.typesafe" % "config" % "1.4.2",

  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion,

  "org.apache.spark" %% "spark-catalyst" % sparkVersion % Test,
  "org.apache.spark" %% "spark-hive" % sparkVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.scalatest" %% "scalatest-funsuite" % "3.2.15" % Test,


)


assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


Test / publishArtifact := false

//ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

ThisBuild / publishTo := {

  publishConfiguration := publishConfiguration.value.withOverwrite(true)
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
  pushRemoteCacheConfiguration := pushRemoteCacheConfiguration.value.withOverwrite(true)

  if (isSnapshot.value) {
    Some(ivyLocal)
  } else {
    None
  }

}
