package io.torchbearer.turkservice.hitprocessing

import io.torchbearer.ServiceCore.DataModel.{Hit, Landmark}
import io.torchbearer.ServiceCore.Orchestration.Task

/**
  * Created by fredricvollmer on 5/11/17.
  */
object VerificationResultProcessor {
  def processVerificationAssignmentsForLandmark(landmark: Landmark, assignments: List[Boolean]): Unit = {
    val score = assignments.foldLeft(0)((s, v) => if (v) s + 1 else s)

    val hit = Hit.getHit(landmark.hitId) getOrElse {
      println("Error processing verification hit result: Hit not found.")
      return
    }

    val taskToken = hit.currentTaskToken getOrElse {
      println("Error processing verification hit result: No task token for hit")
      return
    }

    if ((score.toDouble / assignments.length) > 0.5) {
      // Description was verified!

      // Update Landmark completion
      landmark.updateTurkComplete(true)

      // Check if ALL landmarks for this hit are complete.
      val incompleteLandamrks = Landmark.getLandmarksForHit(landmark.hitId, Some(false))

      if (incompleteLandamrks.isEmpty) {
        // Send task success
        Task.sendSuccess(taskToken, "epId" -> hit.executionPointId, "hitId" -> hit.hitId)
      }
    }

    // This description FAILED
    else {
      Task.sendFailure(taskToken, "MECH_TURK_VALIDATION_ERROR")
    }
  }
}
