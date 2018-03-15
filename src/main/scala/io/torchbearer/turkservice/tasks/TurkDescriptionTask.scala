package io.torchbearer.turkservice.tasks

import io.torchbearer.ServiceCore.DataModel.{Hit, Landmark, LandmarkStatus}
import io.torchbearer.ServiceCore.Orchestration
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory

/**
  * Created by fredricvollmer on 5/10/17.
  */
class TurkDescriptionTask(epId: Int, hitId: Int, taskToken: String)
  extends Orchestration.Task(epId = epId, hitId = hitId, taskToken = taskToken) {

  override def run(): Unit = {
    try {
      println(s"Creating description task for hit $hitId")

      // Get landmarks for hit
      val landmarks = Landmark.getLandmarksForHit(hitId)

      var cost: Int = 0

      landmarks.par.foreach(lm => {
        val task = TurkQuestionFactory.createDescriptionQuestion(lm.landmarkId)

        cost += (task.reward * 100).toInt

        task.submit()

        // Update MechTurkId of internal Hit
        Landmark.updateDescriptionHitIdForLandmark(lm.landmarkId, task.mechTurkHitId)

        // Increment description attempts for landmark
        lm.incrementDescriptionAttempts(1)

        // Update landmark status to pending
        lm.updateStatus(LandmarkStatus.PENDING)

        println(s"Created description task with id ${task.mechTurkHitId} for hit $hitId")
      })

      // Update taskToken for hit in database
      Hit.updateHitWithTask(hitId, taskToken)

      // Update cost
      Hit.incrementCost(hitId, cost)
    }
    catch {
      case e: Throwable => {
        e.printStackTrace()
        sendFailure("Turk Service Error", "Error creating description task")
      }
    }
  }

}
