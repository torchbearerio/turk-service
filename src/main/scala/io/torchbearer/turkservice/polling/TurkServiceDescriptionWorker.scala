package io.torchbearer.turkservice.polling

import akka.actor.ActorSystem
import io.torchbearer.ServiceCore.AWSServices.SFN._
import io.torchbearer.ServiceCore.Constants
import io.torchbearer.turkservice.tasks.{TurkDescriptionTask, TurkSaliencyTask}
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.concurrent._

/**
  * Created by fredricvollmer on 10/30/16.
  */
class TurkServiceDescriptionWorker(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher
  implicit val formats = Serialization.formats(NoTypeHints)

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def run(): Unit = {
    //val descTask = new TurkDescriptionTask(437, 57, "wefewfwf")
    //descTask.run()
    val verificationQuestion = TurkQuestionFactory.createDescriptionVerificationQuestion("f5c2a65c-5ec1-4f7c-8cea-fa74e935017d", "grey house")
    verificationQuestion.submit()

    while (true) {
      val task = getTaskForActivityArn(Constants.ActivityARNs("TURK_DESCRIPTION"))

      // If no tasks were returned, exit
      if (task.getTaskToken != "") {

        val input = parse(task.getInput)
        val epId = (input \ "epId").extract[Int]
        val hitId = (input \ "hitId").extract[Int]
        val taskToken = task.getTaskToken

        val descriptionTask = new TurkDescriptionTask(epId, hitId, taskToken)

        Future {
          descriptionTask.run()
        }
      }
    }
  }


}
