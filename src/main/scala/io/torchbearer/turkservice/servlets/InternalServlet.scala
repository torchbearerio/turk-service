package io.torchbearer.turkservice.servlets

import io.torchbearer.ServiceCore.DataModel.ExecutionPoint
import io.torchbearer.ServiceCore.AWSServices.MechTurk
import io.torchbearer.ServiceCore.TorchbearerDB._
import io.torchbearer.turkservice.TurkServiceStack
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import _root_.akka.actor.ActorSystem
import com.amazonaws.mturk.requester.{EventType, NotificationSpecification, NotificationTransport}

import scala.concurrent.{ExecutionContext, Future}

class InternalServlet(system: ActorSystem) extends TurkServiceStack with JacksonJsonSupport with FutureSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  override protected implicit def executor: ExecutionContext = system.dispatcher

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }


  override def error(handler: ErrorHandler): Unit = ???
}
