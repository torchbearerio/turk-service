package io.torchbearer.turkservice.resources

import io.torchbearer.ServiceCore.DataModel._
import io.torchbearer.ServiceCore.TorchbearerDB._
import io.torchbearer.turkservice.{HitService, TurkServiceStack}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{Accepted, AsyncResult, ErrorHandler, FutureSupport, CorsSupport}
import org.scalatra.json.JacksonJsonSupport
import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

class HitResource(system: ActorSystem) extends TurkServiceStack with JacksonJsonSupport
  with FutureSupport with CorsSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  override protected implicit def executor: ExecutionContext = system.dispatcher

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  /**
    * Respond to preflight requests
    */
  options("/*"){
    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"))
  }

  /********** SALIENCY ***************/

  get("/saliency") {
    val executionPoint = params.get("executionPoint")
    val offset = params.getOrElse("offset", "0").toInt
    val limit = Math.min(params.getOrElse("limit", "100").toInt, 100)
    val expand = params.getOrElse("expand", "false").toBoolean

    new AsyncResult() {
      override val is = Future {
        val total = SaliencyHit.getCount(executionPoint)
        val hits = if (expand) {
          val unexapnededHits = SaliencyHit.getPagedSaliencyHits(offset, limit, executionPoint)
          unexapnededHits.map(h => {
            val newDescriptionHit = h.descriptionHit match {
              case Left(hitId) => hitId.map(x => Right(ObjectDescriptionHit.getDescriptionHit(x))) getOrElse Left(None)
              case Right(hit) => Right(hit)
            }
            h.copy(descriptionHit = newDescriptionHit)
          })
        }

        else {
          SaliencyHit.getPagedSaliencyHits(offset, limit, executionPoint)
        }

        Map(
          "count"  -> total,
          "hits"   -> hits
        )
      }
    }
  }

  get("/saliency/:hitId") {
    val hitId = params("hitId")

    new AsyncResult() {
      override val is = Future {
        SaliencyHit.getSaliencyHit(hitId) getOrElse halt(404, "Hit not found");
      }
    }
  }

  get("/saliency/:hitId/assignments") {
    val hitId: Option[String] = params.get("hitId")
    val offset = params.getOrElse("offset", "0").toInt
    val limit = Math.min(params.getOrElse("limit", "100").toInt, 100)

    new AsyncResult() {
      override val is = Future {
        val total = SaliencyAssignment.getCount(hitId)
        val hits = SaliencyAssignment.getPagedSaliencyAssignments(offset, limit, hitId)
        Map(
          "count"  -> total,
          "assignments"   -> hits
        )
      }
    }
  }

  /********** DESCRIPTION ***************/

  get("/description") {
    val executionPoint = params.get("executionPoint")
    val offset = params.getOrElse("offset", "0").toInt
    val limit = Math.min(params.getOrElse("limit", "100").toInt, 100)
    val expand = params.getOrElse("expand", "false").toBoolean

    new AsyncResult() {
      override val is = Future {
        val total = ObjectDescriptionHit.getCount(executionPoint)
        val hits = if (expand) {
          val unexapnededHits = ObjectDescriptionHit.getPagedDescriptionHits(offset, limit, executionPoint)
          unexapnededHits.map(h => {
            val newDescriptionHit = h.saliencyHit match {
              case Left(hitId) => hitId.map(x => Right(SaliencyHit.getSaliencyHit(x))) getOrElse Left(None)
              case Right(hit) => Right(hit)
            }
            h.copy(saliencyHit = newDescriptionHit)
          })
        }

        else {
          SaliencyHit.getPagedSaliencyHits(offset, limit, executionPoint)
        }
        Map(
          "count"  -> total,
          "hits"   -> hits
        )
      }
    }
  }

  get("/description/:hitId") {
    val hitId = params("hitId")

    new AsyncResult() {
      override val is = Future {
        ObjectDescriptionHit.getDescriptionHit(hitId) getOrElse halt(404, "Hit not found");
      }
    }
  }

  get("/description/:hitId/assignments") {
    val hitId: Option[String] = params.get("hitId")
    val offset = params.getOrElse("offset", "0").toInt
    val limit = Math.min(params.getOrElse("limit", "100").toInt, 100)

    new AsyncResult() {
      override val is = Future {
        val total = ObjectDescriptionAssignment.getCount(hitId)
        val hits = ObjectDescriptionAssignment.getPagedObjectDescriptionAssignments(offset, limit, hitId)
        Map(
          "count"  -> total,
          "assignments"   -> hits
        )
      }
    }
  }

  /**************** Utility *******************/


  override def error(handler: ErrorHandler): Unit = ???
}
