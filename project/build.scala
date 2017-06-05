import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import sbtassembly.AssemblyPlugin._
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}
import com.earldouglas.xwp.JettyPlugin
import com.earldouglas.xwp.JettyPlugin.autoImport._
import com.earldouglas.xwp.ContainerPlugin.autoImport._
import sbtdocker.DockerPlugin.autoImport._
import sbtdocker.DockerKeys.{docker, dockerfile}
import sbtdocker.DockerPlugin

object TurkServiceBuild extends Build {
  val Organization = "io.torchbearer"
  val Name = "Turk Service"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.8"
  val ScalatraVersion = "2.4.1"

  val tsAssemblySettings = assemblySettings ++ Seq(
    // copy web resources to /webapp folder
    resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map {
      (managedBase, base) =>
        val webappBase = base / "src" / "main" / "webapp"
        for {
          (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
        } yield {
          Sync.copy(from, to)
          to
        }
    },
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case PathList("reference.conf") => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assemblyOutputPath in assembly := file("target/build.jar"),
    assemblyJarName in assembly := "build.jar",
    mainClass in assembly := Some("io.torchbearer.turkservice.JettyLauncher")
  )

  // ------------------------------------------
  // Docker Builder
  // ------------------------------------------
  val dockerSettings = dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val artifactTargetPath = s"/target/${artifact.name}"

    new Dockerfile {
      from("java")
      add("build.jar", artifactTargetPath)
      expose(41012)
      entryPoint("java", "-jar", artifactTargetPath)
    }
  }

  lazy val core = ProjectRef(file("../service-core"), "service-core")

  lazy val project = Project(
    "turk-service",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ dockerSettings ++ tsAssemblySettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "Akka Repo" at "http://repo.akka.io/repository",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-library" % ScalaVersion,
        "org.scala-lang" % "scala-reflect" % ScalaVersion,
        "org.scala-lang" % "scala-compiler" % ScalaVersion,
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "container;compile",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "com.typesafe.akka" %% "akka-actor" % "2.3.4",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
        "com.amazonaws" % "aws-java-sdk-osgi" % "1.11.119" withSources(),
        "com.github.haifengl" % "smile-core" % "1.2.0",
        "com.github.haifengl" %% "smile-scala" % "1.2.0",
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s" %% "json4s-jackson" % "3.3.0",
        "xerces" % "xercesImpl" % "2.9.1",
        "ca.juliusdavies" % "not-yet-commons-ssl" % "0.3.9",
        "commons-io" % "commons-io" % "2.5",
        "org.jgrapht" % "jgrapht-core" % "0.9.2",
        "edu.mit" % "jwi" % "2.2.3",
        "net.ettinsmoor" % "java-aws-mturk" % "1.6.2" excludeAll(
          ExclusionRule(organization = "org.apache.commons", name = "not-yet-commons-ssl"),
          ExclusionRule(organization = "apache-xerces", name = "resolver"),
          ExclusionRule(organization = "apache-xerces", name = "xercesImpl"),
          ExclusionRule(organization = "apache-xerces", name = "xml-apis")
          )
      ),
      containerPort in Jetty := 41012,
      javaOptions ++= Seq(
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5002"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile) { base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty, /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ), /* add extra bindings here */
            Some("templates")
          )
        )
      },
      mainClass in(Compile, run) := Some("io.torchbearer.turkservice.JettyLauncher")
    )
  )
    .dependsOn(core)
    .enablePlugins(JettyPlugin)
    .enablePlugins(DockerPlugin)
}
