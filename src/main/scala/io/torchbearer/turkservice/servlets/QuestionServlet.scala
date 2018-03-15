package io.torchbearer.turkservice.servlets

import akka.actor.ActorSystem
import io.torchbearer.ServiceCore.AWSServices.S3
import io.torchbearer.ServiceCore.DataModel.{ExecutionPoint, Hit}
import io.torchbearer.turkservice.{Constants, TurkServiceStack}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{AsyncResult, CorsSupport, ErrorHandler, FutureSupport}

import scala.concurrent.{ExecutionContext, Future}
import io.torchbearer.ServiceCore.{Constants => CoreConstants}

class QuestionServlet(system: ActorSystem) extends TurkServiceStack with FutureSupport with CorsSupport
  with JacksonJsonSupport {

  override protected implicit def executor: ExecutionContext = system.dispatcher

  protected implicit lazy val jsonFormats: Formats = DefaultFormats


  /**
    * Respond to preflight requests
    */
  options("/*") {
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
  }

  /** ******** Object Sampling ***********/

  get(s"/${Constants.SALIENCY_INTERNAL_IDENTIFIER}") {
    val assignmentId = params.getOrElse('assignmentId, 'ASSIGNMENT_ID_NOT_AVAILABLE)
    val hitId = params.get('hitId).map(_.toInt)
      .getOrElse({
        halt(409, "No hit found!")
      })

    new AsyncResult() {
      val is = Future {
        // Get rectangle from saliency hit
        val hit = Hit.getHit(hitId)
          .getOrElse({
            halt(409, "No hit found!")
          })
        val executionPoint = ExecutionPoint.getExecutionPoint(hit.executionPointId)
          .getOrElse({
            halt(409, s"Execution point ${hit.executionPointId} not found!")
          })
        val instruction = executionPoint.executionPointType match {
          case CoreConstants.EXECUTION_POINT_TYPE_MANEUVER =>
            "Imagine you are telling someone who is driving to turn at this intersection. " +
            "The images below show the approach to an intersection or turn from different distances." +
            "In each of the three images below, draw a box just around the landmark you would use to describe this location--" +
              "the <strong>most obvious, most stand-out, most important, and/or easiest-to-see</strong> object in the image." +
              "<br/> " +
              "<strong style='color:red !important;'>DON'T select objects which are temporary, such as people or cars.</strong>"
          case CoreConstants.EXECUTION_POINT_TYPE_DESTINATION_RIGHT
               | CoreConstants.EXECUTION_POINT_TYPE_DESTINATION_LEFT =>
            "Draw a box just around the main feature of this image--" +
              "the landmark or object that is most <strong>obvious</strong> or <strong>important</strong>."
        }

        contentType = "text/html"
        mustache("/saliency.mustache",
          "instruction" -> instruction,
          "assignmentId" -> assignmentId,
          "submitURL" -> Constants.EXTERNAL_QUESTION_SUBMIT_URL,
          "streetviewImgURLAt" -> getSVImageURL(hit.executionPointId, CoreConstants.POSITION_AT).orNull,
          "streetviewImgURLJustBefore" -> getSVImageURL(hit.executionPointId, CoreConstants.POSITION_JUST_BEFORE).orNull,
          "streetviewImgURLBefore" -> getSVImageURL(hit.executionPointId, CoreConstants.POSITION_BEFORE).orNull
        )
      }
    }
  }

  override def error(handler: ErrorHandler): Unit = ???

  private def getSVImageURL(epId: Int, position: String): Option[String] = {
    val s3 = S3.getClient
    val b = CoreConstants.S3_SV_IMAGE_BUCKET
    val key = s"${epId}_$position.jpg"
    if (s3.doesObjectExist(b, key)) {
      Some(s"${Constants.STREETVIEW_IMAGES_BASE_URL}/${epId}_$position.jpg")
    }
    else {
      None
    }
  }
}
