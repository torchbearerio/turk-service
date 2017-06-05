package io.torchbearer.turkservice.polling

import akka.actor.ActorSystem
import io.torchbearer.ServiceCore.AWSServices.SFN._
import io.torchbearer.ServiceCore.Constants
import io.torchbearer.turkservice.tasks.TurkSaliencyTask
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.concurrent._

/**
  * Created by fredricvollmer on 10/30/16.
  */
class TurkServiceSaliencyWorker(system: ActorSystem) extends Runnable {
  protected implicit def executor: ExecutionContext = system.dispatcher
  implicit val formats = Serialization.formats(NoTypeHints)

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def run(): Unit = {
    //val saliencyTask = new TurkSaliencyTask(437, 57, "wefewfwf")
    //saliencyTask.run()

    while (true) {
      val task = getTaskForActivityArn(Constants.ActivityARNs("TURK_SALIENCY"))

      // If no tasks were returned, exit
      if (task.getTaskToken != "") {

        val input = parse(task.getInput)
        val epId = (input \ "epId").extract[Int]
        val hitId = (input \ "hitId").extract[Int]
        val taskToken = task.getTaskToken

        val saliencyTask = new TurkSaliencyTask(epId, hitId, taskToken)

        Future {
          saliencyTask.run()
        }
      }
    }
  }


}
