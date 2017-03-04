package io.torchbearer.turkservice.tasks

import akka.actor.ActorSystem
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message, ReceiveMessageRequest}
import io.torchbearer.ServiceCore.AWSServices.SQS
import io.torchbearer.ServiceCore.DataModel.{ObjectDescriptionAssignment, SaliencyAssignment, Hit}
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.turkservice.DescriptionResultProcessor._
import io.torchbearer.turkservice.DescriptionResultProcessor._
import io.torchbearer.turkservice.{Constants, DescriptionResultProcessor, SaliencyResultProcessor, TurkClientFactory}
import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Created by fredricvollmer on 10/30/16.
  */
class CompletedHitPollingTask(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher

  implicit val formats = DefaultFormats

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val turkClient = TurkClientFactory.getClient

  // Build SQS client
  private val sqsClient = SQS.getClient
  private val request = new ReceiveMessageRequest(Constants.SQS_HIT_COMPLETION_URL)
  request.setWaitTimeSeconds(20)

  logger.debug("SQS client built for hit completion.")

  override def run(): Unit = {
    while (true) {
      processSQSMessages()
    }
  }

  private def processSQSMessages(): Unit = {
    log("Polling task running: HITS.")
    val messages: Seq[Message] = sqsClient.receiveMessage(request).getMessages
    log(s"Received ${messages.length} hit completion messages")

    messages.foreach((m: Message) => {
      val handle = m.getReceiptHandle
      val events = (parse(m.getBody) \ "Events").extract[List[Map[String, String]]]

      events.foreach(e => {
        Future {
          val hitId = e("HITId")
          val hitTypeId = e("HITTypeId")

          val turkAssignemnts = turkClient.getAllAssignmentsForHIT(hitId)

          hitTypeId match {
              // Description hit
            case "38ZP8AG6P150VRITKZNR8GEKZGYWI2" => {
              // Retrieve internal hit using the MTurk hitIf
              val hit = Hit.getHitByDescriptionHitId(hitId) getOrElse {
                throw new IllegalArgumentException(s"Unable to load hit for external hitId $hitId")
              }

              // Map assignments to List of (key, value) maps
              val assignments = turkAssignemnts.map(m => {
                val answerXML = scala.xml.XML.loadString(m.getAnswer) \ "Answer"
                val descriptors = answerXML.map(a => {
                  val k = (a \ "QuestionIdentifier").text
                  val v = (a \ "FreeText").text
                  (k, v)
                }).toMap
                new ObjectDescriptionAssignment(m.getAssignmentId, hit.hitId, 0, Some(descriptors), Some(m.getWorkerId), None)
              })
              DescriptionResultProcessor.processDescriptionAssignmentsForHit(hit, assignments.toList)
            }

              // Saliency hit
            case Constants.SALIENCY_HIT_TYPE_ID => {
              // Retrieve internal hit using the MTurk hitIf
              val hit = Hit.getHitBySaliencyHitId(hitId) getOrElse {
                throw new IllegalArgumentException(s"Unable to load hit for external hitId $hitId")
              }

              // Map assignments to List of (key, value) maps
              val assignments = turkAssignemnts.map(m => {
                val answerXML = scala.xml.XML.loadString(m.getAnswer) \ "Answer"
                val rectPoints = answerXML.map(a => {
                  val k = (a \ "QuestionIdentifier").text
                  val v = (a \ "FreeText").text
                  (k, v)
                }).toMap
                new SaliencyAssignment(m.getAssignmentId, hit.hitId, 0, Some(Rectangle(rectPoints)), Some(m.getWorkerId), None)
              })
              SaliencyResultProcessor.processSaliencyAssignemntsForHit(hit, assignments.toList)
            }
          }
        }
      })

      // Delete message from queue
      Future {
        val deleteReq = new DeleteMessageRequest(Constants.SQS_HIT_COMPLETION_URL, handle)
        sqsClient.deleteMessage(deleteReq)
      }
    })

    log("Polling task complete.")
  }

  private def log(message: String) = {
    println("PollingService: " + message)
  }
}
