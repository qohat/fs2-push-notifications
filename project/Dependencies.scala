import sbt._

object Dependencies {
  object Version {
    val fs2Rabit = "5.0.0"
    val cats     = "3.2.9"

    val log4cats         = "2.1.1"
    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val logback          = "1.2.6"
    val organizeImports  = "0.5.0"
    val semanticDB       = "4.4.30"
  }

  object Libraries {
    def fs2Rabbit(artifact: String): ModuleID =
      "dev.profunktor" %% artifact % Version.fs2Rabit withSources () withJavadoc ()

    def cats(artifact: String): ModuleID = "org.typelevel" %% artifact % Version.cats withSources () withJavadoc ()

    val fs2RabbitClient = fs2Rabbit("fs2-rabbit")
    val fs2RabbitCirce  = fs2Rabbit("fs2-rabbit-circe")
    val catsEffect      = cats("cats-effect")

    // Runtime
    val logback         = "ch.qos.logback"        % "logback-classic"  % Version.logback
    val log4cats        = "org.typelevel"        %% "log4cats-slf4j"   % Version.log4cats
    val organizeImports = "com.github.liancheng" %% "organize-imports" % Version.organizeImports
  }

  object CompilerPlugin {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % Version.betterMonadicFor
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % Version.kindProjector cross CrossVersion.full
    )
    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % Version.semanticDB cross CrossVersion.full
    )
  }

}
