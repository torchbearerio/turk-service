package io.torchbearer.turkservice.hitprocessing

import io.torchbearer.ServiceCore.DataModel.SaliencyAssignment._
import io.torchbearer.ServiceCore.DataModel._
import io.torchbearer.ServiceCore.Orchestration.Task
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import org.jgrapht.alg.BronKerboschCliqueFinder
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions._

/**
  * Created by fredvollmer on 1/22/17.
  */

object SaliencyResultProcessor {

  def processSaliencyAssignemntsForHit(hit: Hit, assignments: List[SaliencyAssignment]): Unit = {
    val rects = assignments.flatMap(_.rectangle)

    val G = Rectangle.createRectangleGraph(rects)
    val cf = new BronKerboschCliqueFinder[Rectangle, DefaultEdge](G)

    val cliques = cf.getAllMaximalCliques

    // Reduce each clique of rects down to a single rect
    val reducedRects = cliques.map(clique => {
      // Sum rectangle measurements
      val rect = clique.foldLeft(Rectangle(0, 0, 0, 0))((result, rect) => {
        Rectangle(
          result.x1 + rect.x1,
          result.x2 + rect.x2,
          result.y1 + rect.y1,
          result.y2 + rect.y2
        )
      })

      // Take average
      rect.x1 /= clique.size.toDouble
      rect.x2 /= clique.size.toDouble
      rect.y1 /= clique.size.toDouble
      rect.y2 /= clique.size.toDouble

      // Return tuple of (clique size, rect)
      (clique.size, rect)
    })

    // Save assignments to DB
    insertSaliencyAssignments(assignments)

    // Create Landmarks from rects
    for ((cliqueSize, rect) <- reducedRects) {
      Landmark.insertLandmark(hit.hitId, Some(rect), Some(cliqueSize))
    }

    // Send task success
    val taskToken = hit.currentTaskToken map
      (t => Task sendSuccess(t, "epId" -> hit.executionPointId, "hitId" -> hit.hitId))
  }
}
