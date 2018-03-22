package io.torchbearer.turkservice.hitprocessing

import io.torchbearer.ServiceCore.DataModel.SaliencyAssignment._
import io.torchbearer.ServiceCore.DataModel._
import io.torchbearer.ServiceCore.Orchestration.Task
import io.torchbearer.ServiceCore.tyoes.Rectangle
import io.torchbearer.ServiceCore.{Constants => CoreConstants}
import org.jgrapht.alg.BronKerboschCliqueFinder
import org.jgrapht.graph.DefaultEdge
import breeze.linalg._
import breeze.numerics._
import com.amazonaws.services.s3.model.ObjectMetadata
import io.torchbearer.ServiceCore.AWSServices.S3
import org.apache.commons.io.IOUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.json4s.jackson.Serialization
import scala.collection.JavaConversions._

/**
  * Created by fredvollmer on 1/22/17.
  */

object SaliencyResultProcessor {
  implicit val formats = Serialization.formats(NoTypeHints)

  def processSaliencyAssignemntsForHit(hit: Hit, assignments: List[SaliencyAssignment]): Unit = {
    val atRects = assignments.flatMap(_.atRectangle)
    val beforeRects = assignments.flatMap(_.beforeRectangle)
    val justBeforeRects = assignments.flatMap(_.justBeforeRectangle)

    try {
      print(s"Processing ${assignments.size} assignments for hit ${hit.hitId}")

      for ((position, rects) <- Seq(
        (CoreConstants.POSITION_AT, atRects),
        (CoreConstants.POSITION_JUST_BEFORE, justBeforeRects),
        (CoreConstants.POSITION_BEFORE, beforeRects)
      )) {

        // Build matrix representing saliency map
        val (w, h) = CoreConstants.SV_IMAGE_DIMS
        var saliencyMap = DenseMatrix.zeros[Int](h, w)

        // For each rectangle, increment the elements in that sub-matrix of the saliency map
        rects.foreach(rect => {
          saliencyMap(rect.y1 to rect.y2, rect.x1 to rect.x2) :+= 1
        })

        // Normalize the saliency map between 0 and 255
        val salMin = min(saliencyMap)
        val salMax = max(saliencyMap)

        saliencyMap = saliencyMap.map(x => {
          (x - salMin) / (salMax - salMin) * 255
        })

        // Hahaha, what I'm about to do is stupid: we need to turn this into seq of seq for serialization.
        // Map row by row
        val json = "saliencyMatrix" -> saliencyMap(*, ::).map(_.toArray).toArray

        val smJson = write(json)
        val jsonStream = IOUtils.toInputStream(smJson, "UTF-8")
        val metadata = new ObjectMetadata()
        metadata.setContentType("application/json")
        metadata.setContentLength(jsonStream.available)

        // Save saliency map to S3
        S3.getClient.putObject(CoreConstants.S3_SALIENCY_MAP_BUCKET,
          s"${hit.hitId}_$position.json",
          jsonStream,
          metadata)
      }

      // Save assignments to DB
      insertSaliencyAssignments(assignments)

      // Send task success
      hit.currentTaskToken foreach
        (t => Task sendSuccess(t, "epId" -> hit.executionPointId, "hitId" -> hit.hitId))

      println(s"Saliency task complete for hit ${hit.hitId}")
    }
    catch {
      case e: Throwable => hit.currentTaskToken map
        (t => {
          e.printStackTrace()
          Task sendFailure(t, "Turk Service Error", e.getMessage)
        })
    }

    finally {
      Hit.setEndTimeForTask(hit.hitId, "turk_saliency", System.currentTimeMillis)
    }

    // NOTE: This legacy code finds landmarks by building a graph from the rectangles
    // and then finding maximal cliques in that graph. It works well and is intuitive,
    // but the non-maximal suppression approach is more standard.

    /*
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
    */
  }
}
