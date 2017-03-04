package io.torchbearer.turkservice
import io.torchbearer.ServiceCore.DataModel._
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import io.torchbearer.ServiceCore.DataModel.SaliencyAssignment._
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.turkservice.filters.{AverageRectangleReducer, SaliencyFilterTrait, RectangleOverlapFilter}

/**
  * Created by fredvollmer on 1/22/17.
  */

object DescriptionResultProcessor {

  def processDescriptionAssignmentsForHit(hit: Hit, assignments: List[ObjectDescriptionAssignment]): Unit = {
    val descriptors = assignments.flatMap(_.description)
    val filters = List(
      Class.forName(s"${Constants.FILTER_PACKAGE}.DescriptorFilter")
    )

    // Save assignments to DB
    ObjectDescriptionAssignment.insertDescriptionAssignments(assignments)

    // Run results through filters
    var filteredDescriptors = descriptors

    filters.foreach(f => {
      val filter = f.getDeclaredConstructor(classOf[List[Map[String, String]]]).newInstance(filteredDescriptors)
      filteredDescriptors = filter.getClass.getDeclaredMethod("runFilter").invoke(filter)
        .asInstanceOf[List[Map[String, String]]]
    })

    val finalDescriptorSet = filteredDescriptors.head

    // Update Hit in DB
    hit.updateComputedDescription(finalDescriptorSet)
    hit.updateStatus(CoreConstants.HIT_STATUS_BETWEEN_HITS)
  }
}
