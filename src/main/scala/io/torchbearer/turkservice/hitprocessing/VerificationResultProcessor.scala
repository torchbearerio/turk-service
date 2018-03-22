package io.torchbearer.turkservice.hitprocessing

import io.torchbearer.ServiceCore.DataModel.{Hit, Landmark, LandmarkStatus}
import io.torchbearer.ServiceCore.Orchestration.Task
import io.torchbearer.turkservice.Constants
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory

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
      landmark.updateStatus(LandmarkStatus.VERIFIED)
    }

    // This description FAILED
    else {
      println(s"Verification failed for hit ${hit.hitId}, epID ${hit.executionPointId}")

      // If we haven't hit max description attempts, try, try again
      if (landmark.turkDescriptionAttempts < Constants.MAX_DESCRIPTION_ATTEMPTS) {
        val task = TurkQuestionFactory.createDescriptionQuestion(landmark.landmarkId)

        task.submit()

        // Update description attempts for this landmark
        landmark.incrementDescriptionAttempts(1)

        // Update MechTurkId of internal Hit
        Landmark.updateDescriptionHitIdForLandmark(landmark.landmarkId, task.mechTurkHitId)

        // Update Hit cost
        Hit.incrementCost(landmark.hitId, (task.reward * 100).toInt)

        println(s"Created RETRY description task with id ${task.mechTurkHitId} for hit ${landmark.hitId}")
      }
      else {
        landmark.updateStatus(LandmarkStatus.FAILED)
      }
    }

    // Check if ALL landmarks for this hit are complete (verified or failed).
    // Here we retrieve all landmarks which are still in a pending or unknown status
    val incompleteLandamrks = Landmark.getLandmarksForHit(landmark.hitId, Some(Seq(LandmarkStatus.UNKNOWN, LandmarkStatus.PENDING)))

    if (incompleteLandamrks.isEmpty) {
      // Send task success
      Task.sendSuccess(taskToken, "epId" -> hit.executionPointId, "hitId" -> hit.hitId)

      // Update verification task end time
      Hit.setEndTimeForTask(hit.hitId, "turk_verification", System.currentTimeMillis)
    }
  }
}
