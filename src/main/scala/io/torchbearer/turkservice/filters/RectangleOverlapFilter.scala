package io.torchbearer.turkservice.filters

import io.torchbearer.ServiceCore.tyoes.Rectangle
import org.jgrapht.alg.BronKerboschCliqueFinder
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions._

/**
  * Created by fredvollmer on 1/22/17.
  */
class RectangleOverlapFilter(rects: List[Rectangle]) extends SaliencyFilterTrait {
  private def getLargestCluster: List[Rectangle] = {
    val G = Rectangle.createRectangleGraph(rects)
    val cf = new BronKerboschCliqueFinder[Rectangle, DefaultEdge](G)

    val cliques = cf.getBiggestMaximalCliques

    // Randomly choose one
    cliques.iterator.next.toList
  }

  override def runFilter(): List[Rectangle] = getLargestCluster
}
