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

    val assignment = assignments.head
    val description = assignment.description getOrElse ""

    // Save assignments to DB
    ObjectDescriptionAssignment.insertDescriptionAssignments(assignments)

    // Update Landmark in DB
    // Pass tuple of (description from turk, 1.0) since we are "entirely confident" in this answer
    landmark.updateDescription((description, 1.0))

    // Initiate verification hit on MechTurk
    val verificationQuestion = TurkQuestionFactory.createDescriptionVerificationQuestion(landmark.landmarkId, description)
    verificationQuestion.submit()
  }
}