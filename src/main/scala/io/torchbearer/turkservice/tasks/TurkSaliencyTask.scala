package io.torchbearer.turkservice.tasks

import io.torchbearer.ServiceCore.DataModel.Hit
import io.torchbearer.ServiceCore.Orchestration
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory

/**
  * Created by fredricvollmer on 5/10/17.
  */
class TurkSaliencyTask(epId: Int, hitId: Int, taskToken: String)
  extends Orchestration.Task(epId = epId, hitId = hitId, taskToken = taskToken) {

  override def run(): Unit = {
    try {
      println(s"Creating saliency task for hit $hitId")

      Hit.setStartTimeForTask(hitId, "turk_saliency", System.currentTimeMillis)

      val task = TurkQuestionFactory.createSaliencyQuestion(epId, hitId)
      task.submit()

      // Update MechTurkId of internal Hit
      Hit.updateSaliencyHitIdForHit(hitId, task.mechTurkHitId)

      // Update taskToken for hit in database, so we can send success once we get answer
      Hit.updateHitWithTask(hitId, taskToken)

      // Update cost
      Hit.incrementCost(hitId, (task.reward * 100).toInt)

      println(s"Created saliency task with id ${task.mechTurkHitId} for hit $hitId")
    }
    catch {
      case e: Throwable => {
        e.printStackTrace()
        sendFailure("Turk Service error", e.getMessage)
      }
    }
  }
}
