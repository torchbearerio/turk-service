package io.torchbearer.turkservice.servlets

import io.torchbearer.ServiceCore.DataModel.ExecutionPoint
import io.torchbearer.ServiceCore.TorchbearerDB._
import io.torchbearer.turkservice.{HitService, TurkClientFactory, TurkServiceStack}
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

  post("/sendtestnotification") {
    //Future {
      val turkClient = TurkClientFactory.getClient
      val notification = new NotificationSpecification("https://sqs.us-west-2.amazonaws.com/814009652816/completed-hits",
        NotificationTransport.SQS, "2006-05-05", Array(EventType.HITReviewable))
      turkClient.sendTestEventNotification(notification, EventType.HITReviewable)
    //}

    //Ok()
  }

  override def error(handler: ErrorHandler): Unit = ???
}
