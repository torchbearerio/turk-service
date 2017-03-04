package io.torchbearer.turkservice

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  // this is my entry object as specified in sbt project definition
  def main(args: Array[String]) {
    val port = sys.env.getOrElse("PORT", "41012").toInt
    val rb = getClass.getClassLoader.getResource("webapp").toExternalForm

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase(rb)
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}