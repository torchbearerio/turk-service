package io.torchbearer.turkservice.tasks

import io.torchbearer.ServiceCore.DataModel.{Hit, Landmark}
import io.torchbearer.ServiceCore.Orchestration
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory

/**
  * Created by fredricvollmer on 5/10/17.
  */
class TurkDescriptionTask(epId: Int, hitId: Int, taskToken: String)
  extends Orchestration.Task(epId = epId, hitId = hitId, taskToken = taskToken) {

  override def run(): Unit = {
    try {
      // Get landmarks for hit
      val landmarks = Landmark.getLandmarksForHit(hitId)

      landmarks.par.foreach(lm => {
        val task = TurkQuestionFactory.createDescriptionQuestion(lm.landmarkId)

        task.submit()

        // Update MechTurkId of internal Hit
        Landmark.updateDescriptionHitIdForLandmark(lm.landmarkId, task.mechTurkHitId)
      })

      // Update taskToken for hit in database
      Hit.updateTaskTokenForHit(hitId, taskToken)
    }
    catch {
      case e: Throwable => {
        e.printStackTrace()
        sendFailure("Turk Service Error", "Error creating saliency task")
      }
    }
  }

}
