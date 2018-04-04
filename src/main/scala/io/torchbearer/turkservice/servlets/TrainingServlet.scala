package io.torchbearer.turkservice.servlets

import akka.actor.ActorSystem
import io.torchbearer.ServiceCore.AWSServices.S3
import io.torchbearer.ServiceCore.DataModel.{ExecutionPoint, Hit}
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import io.torchbearer.turkservice.{Constants, TurkServiceStack}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{AsyncResult, CorsSupport, ErrorHandler, FutureSupport}

import scala.concurrent.{ExecutionContext, Future}

class TrainingServlet(system: ActorSystem) extends TurkServiceStack {

  /** ******** Description ***********/

  get(s"/${Constants.DESCRIPTION_INTERNAL_IDENTIFIER}") {
    contentType = "text/html"
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.

    mustache("/description_qualifier.mustache")
  }

  /** ******** Saliency ***********/

  get(s"/${Constants.SALIENCY_INTERNAL_IDENTIFIER}") {
    contentType = "text/html"
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.

    mustache("/saliency_qualifier.mustache")
  }

  override def error(handler: ErrorHandler): Unit = ???
}
