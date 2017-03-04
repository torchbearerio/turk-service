package io.torchbearer.turkservice.tasks

import akka.actor.ActorSystem
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message, ReceiveMessageRequest}
import io.torchbearer.ServiceCore.AWSServices.SQS
import io.torchbearer.ServiceCore.DataModel.{ExecutionPoint, Hit}
import io.torchbearer.turkservice.Constants
import io.torchbearer.turkservice.HitService.initiateHit
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by fredricvollmer on 10/30/16.
  */
class ProcessingPollingTask(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  // Build SQS client
  private val sqs = SQS.getClient
  private val request = new ReceiveMessageRequest(Constants.SQS_PROCESSING_URL)
  request.setWaitTimeSeconds(20)

  logger.debug("SQS client built for processing.")

  override def run(): Unit = {
    while (true) {
      requestSQSMessages()
    }
  }

  private def requestSQSMessages(): Unit = {
    implicit val formats = DefaultFormats

    log("Polling task running.")
    val messages: Seq[Message] = sqs.receiveMessage(request).getMessages
    log(s"Received ${messages.length} processing messages")

    messages.par.foreach(m => {
      val body = parse(m.getBody)
      val epId = (body \ "executionPointId").extract[String]
      val saliencyReward = (body \ "saliencyReward").extract[Int]
      val descriptionReward = (body \ "descriptionReward").extract[Int]
      val saliencyAssignemntCount = (body \ "saliencyAssignmentCount").extract[Int]
      val descriptionAssignemntCount = (body \ "descriptionAssignmentCount").extract[Int]
      val distance = (body \ "hitDistance").extract[Int]

      val hit = Hit(epId, saliencyReward, descriptionReward, saliencyAssignemntCount, descriptionAssignemntCount,
        distance)

      // Process HIT
      Future {
        initiateHit(hit)
      }
    })

    // Delete messages
    messages.par.foreach((m: Message) => {
      val handle = m.getReceiptHandle

      // Delete message from queue
      val deleteReq = new DeleteMessageRequest(Constants.SQS_PROCESSING_URL, handle)
      sqs.deleteMessage(deleteReq)
    })

    log("Polling task complete.")
  }

  private def log(message: String) = {
    println("PollingService: " + message)
  }
}
