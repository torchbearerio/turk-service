package io.torchbearer.turkservice
import io.torchbearer.ServiceCore.DataModel.{ExecutionPoint, Hit, SaliencyAssignment, SaliencyHit}
import io.torchbearer.ServiceCore.DataModel.SaliencyAssignment._
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.turkservice.filters.{AverageRectangleReducer, RectangleOverlapFilter, SaliencyFilterTrait}

/**
  * Created by fredvollmer on 1/22/17.
  */

object SaliencyResultProcessor {

  def processSaliencyAssignemntsForHit(hit: Hit, assignments: List[SaliencyAssignment]): Unit = {
      val rects = assignments.flatMap(_.rectangle)
      val filters = List(
        Class.forName(s"${Constants.FILTER_PACKAGE}.RectangleOverlapFilter"),
        Class.forName(s"${Constants.FILTER_PACKAGE}.AverageRectangleReducer")
      )

    // Save assignments to DB
    insertSaliencyAssignments(assignments)

    // Run results through filters
    var filteredRects = rects

    filters.foreach(f => {
      val filter = f.getDeclaredConstructor(classOf[List[Rectangle]]).newInstance(filteredRects)
      filteredRects = filter.getClass.getDeclaredMethod("runFilter").invoke(filter).asInstanceOf[List[Rectangle]]
    })

    val finalRect = filteredRects.head

    // Update Hit in DB
    hit.updateComputedRectangle(finalRect)
    hit.updateStatus(CoreConstants.HIT_STATUS_BETWEEN_HITS)

    // Initiate description hit
    HitService.runNextHitType(hit)
  }
}
