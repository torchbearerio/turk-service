package io.torchbearer.turkservice.hitprocessing

import io.torchbearer.ServiceCore.DataModel._
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import io.torchbearer.turkservice.turkquestions.TurkQuestionFactory

/**
  * Created by fredvollmer on 1/22/17.
  */

object DescriptionResultProcessor {

  def processDescriptionAssignmentsForLandmark(landmark: Landmark, assignments: List[ObjectDescriptionAssignment]): Unit = {
    // We're leaving this method to accept multiple assignments, even though this makes no sense
    // under current scheme. There should only be one assignment in the list passed to this method.
    // This is to allow for multi-assignment descriptions in future.

    /*
    val descriptors = assignments.flatMap(_.description)

    val filters = List(
      Class.forName(s"${Constants.FILTER_PACKAGE}.DescriptorFilter")
    )

    // Run results through filters
    var filteredDescriptors = descriptors

    filters.foreach(f => {
      val filter = f.getDeclaredConstructor(classOf[List[Map[String, String]]]).newInstance(filteredDescriptors)
      filteredDescriptors = filter.getClass.getDeclaredMethod("runFilter").invoke(filter)
        .asInstanceOf[List[Map[String, String]]]
    })

    val finalDescriptorSet = filteredDescriptors.head
    */

    // There willl always only be one assignment
    val assignment = assignments.head
    val description = assignment.description getOrElse ""

    // Save assignments to DB
    ObjectDescriptionAssignment.insertDescriptionAssignments(assignments)

    // Update Landmark in DB
    landmark.updateDescription(description)

    // Initiate verification hit on MechTurk
    val verificationQuestion = TurkQuestionFactory.createDescriptionVerificationQuestion(landmark.landmarkId, description)
    verificationQuestion.submit()

    // Update hit cost
    Hit.incrementCost(landmark.hitId, (verificationQuestion.reward * 100).toInt)

    // Update end time of description task
    Hit.setEndTimeForTask(landmark.hitId, "turk_description", System.currentTimeMillis)

    // Update start time of verification task
    Hit.setStartTimeForTask(landmark.hitId, "turk_verification", System.currentTimeMillis)

    // Update landmark with this verification external hit id
    Landmark.updateVerificationHitIdForLandmark(landmark.landmarkId, verificationQuestion.mechTurkHitId)
  }
}
