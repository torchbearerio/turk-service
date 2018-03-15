package io.torchbearer.turkservice.polling

import akka.actor.ActorSystem
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message, ReceiveMessageRequest}
import io.torchbearer.ServiceCore.AWSServices.{MechTurk, SQS}
import io.torchbearer.ServiceCore.AWSServices.MechTurk._
import io.torchbearer.ServiceCore.DataModel.{Hit, Landmark, ObjectDescriptionAssignment, SaliencyAssignment}
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.turkservice._
import io.torchbearer.turkservice.hitprocessing.VerificationResultProcessor
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * Created by fredricvollmer on 10/30/16.
  */
class CompletedVerificationHitPollingWorker(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher

  implicit val formats = DefaultFormats

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val turkClient = MechTurk.getClient

  // Build SQS client
  private val sqsClient = SQS.getClient
  private val request = new ReceiveMessageRequest(Constants.SQS_HIT_VERIFICATION_URL)
  request.setWaitTimeSeconds(20)

  log("SQS client built for verification hit completion.")

  override def run(): Unit = {
    while (true) {
      processSQSMessages()
    }
  }

  private def processSQSMessages(): Unit = {
    val messages: Seq[Message] = sqsClient.receiveMessage(request).getMessages
    if (messages.nonEmpty)
      log(s"Received ${messages.length} hit completion messages")

    messages.foreach((m: Message) => {
      val handle = m.getReceiptHandle
      val events = (parse(m.getBody) \ "Events").extract[List[Map[String, String]]]

      events.foreach(e => {
        Future {
          val hitId = e("HITId")

          val turkAssignemnts = turkClient.getAllAssignmentsForHIT(hitId)

          // Retrieve Landmark corresponding to Hit
          val landmark = Landmark.getLandmarkByVerificationHitId(hitId) getOrElse {
            println(s"Unable to load landmark for external hitId $hitId")
            return
          }

          val assignments = turkAssignemnts.map(m => {
            val answerXML = scala.xml.XML.loadString(m.getAnswer) \ "Answer"
            val vote = (answerXML.last \ "SelectionIdentifier").text
            vote.toBoolean
          }).toList

          VerificationResultProcessor.processVerificationAssignmentsForLandmark(landmark, assignments)
        }
      })

      // Delete message from queue
      Future {
        blocking {
          val deleteReq = new DeleteMessageRequest(Constants.SQS_HIT_VERIFICATION_URL, handle)
          sqsClient.deleteMessage(deleteReq)
        }
      }
    })
  }

  private def log(message: String) = {
    println("PollingService: " + message)
  }
}
