package io.torchbearer.turkservice.polling

import akka.actor.ActorSystem
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message, ReceiveMessageRequest}
import io.torchbearer.ServiceCore.AWSServices.{SQS, MechTurk}
import io.torchbearer.ServiceCore.AWSServices.MechTurk._
import io.torchbearer.ServiceCore.DataModel.{Hit, ObjectDescriptionAssignment, SaliencyAssignment}
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.turkservice.hitprocessing.DescriptionResultProcessor._
import io.torchbearer.turkservice.hitprocessing.DescriptionResultProcessor._
import io.torchbearer.turkservice.hitprocessing.SaliencyResultProcessor
import io.torchbearer.turkservice.Constants
import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future, blocking}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Created by fredricvollmer on 10/30/16.
  */
class CompletedSaliencyHitPollingWorker(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher

  implicit val formats = DefaultFormats

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val turkClient = MechTurk.getClient

  // Build SQS client
  private val sqsClient = SQS.getClient
  private val request = new ReceiveMessageRequest(Constants.SQS_HIT_SALIENCY_URL)
  request.setWaitTimeSeconds(20)

  log("SQS client built for saliency hit completion.")

  override def run(): Unit = {
    while (true) {
      processSQSMessages()
    }
  }

  private def processSQSMessages(): Unit = {
    val messages: Seq[Message] = sqsClient.receiveMessage(request).getMessages
    if (messages.nonEmpty)
      log(s"Received ${messages.length} saliency hit completion messages")

    messages.foreach((m: Message) => {
      val handle = m.getReceiptHandle
      val events = (parse(m.getBody) \ "Events").extract[List[Map[String, String]]]

      events.foreach(e => {
        Future {
          val hitId = e("HITId")

          val turkAssignemnts = turkClient.getAllAssignmentsForHIT(hitId)

          // Retrieve internal hit using the MTurk hitId
          val hit = Hit.getHitBySaliencyHitId(hitId) getOrElse {
            println(s"Unable to load hit for external hitId $hitId")
            return
          }

          // Map assignments to List of (key, value) maps
          val assignments = turkAssignemnts.map(m => {
            val answerXML = scala.xml.XML.loadString(m.getAnswer) \ "Answer"
            val rectPoints = answerXML.map(a => {
              val k = (a \ "QuestionIdentifier").text
              val v = (a \ "FreeText").text.toInt
              (k, v)
            }).toMap

            val atRect = Rectangle(rectPoints("x1_at"), rectPoints("x2_at"), rectPoints("y1_at"), rectPoints("y2_at"))
            val beforeRect = Rectangle(rectPoints("x1_before"), rectPoints("x2_before"), rectPoints("y1_before"),
              rectPoints("y2_before"))
            val justBeforeRect = Rectangle(rectPoints("x1_justBefore"), rectPoints("x2_justBefore"),
              rectPoints("y1_justBefore"), rectPoints("y2_justBefore"))

            val duration = m.getAcceptTime.getTime - m.getSubmitTime.getTime

            new SaliencyAssignment(m.getAssignmentId, hit.hitId, duration,
              Some(atRect),
              Some(justBeforeRect),
              Some(beforeRect),
              Some(m.getWorkerId), None)
          })
          SaliencyResultProcessor.processSaliencyAssignemntsForHit(hit, assignments)
        }
      })

      // Delete message from queue
      Future {
        blocking {
          val deleteReq = new DeleteMessageRequest(Constants.SQS_HIT_SALIENCY_URL, handle)
          sqsClient.deleteMessage(deleteReq)
        }
      }
    })
  }

  private def log(message: String) = {
    println("PollingService: " + message)
  }
}
