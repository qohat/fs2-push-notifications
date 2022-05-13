import Dependencies._
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "1.0.0"
ThisBuild / organization     := "com.qohat"
ThisBuild / organizationName := "com.qohat"

ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalafixDependencies += Libraries.organizeImports

resolvers += Resolver.sonatypeRepo("snapshots")

val scalafixCommonSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name                 := "fs2-rabbit-push-notifications",
    Docker / packageName := "fs2-rabbit-push-notifications",
    scalafmtOnCompile    := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    scalafixCommonSettings,
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts     := Seq(),
    dockerUpdateLatest := true,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    scalafixCommonSettings,
    libraryDependencies ++= Seq(
      Libraries.log4cats,
      Libraries.logback % Runtime,
      Libraries.fs2RabbitClient,
      Libraries.fs2RabbitCirce,
      Libraries.catsEffect
    )
  )
