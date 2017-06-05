package io.torchbearer.turkservice.filters

import io.torchbearer.ServiceCore.tyoes.Rectangle
import org.jgrapht.alg.BronKerboschCliqueFinder
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * Created by fredvollmer on 1/22/17.
  */
class RectangleOverlapFilter(rects: List[Set[Rectangle]]) extends SaliencyFilterTrait {
  private def getClusters: List[Set[Rectangle]] = {
    // Work only with first set of rectangles (should only be one, as this is first filter)
    val rectSet = rects.head.toList

    val G = Rectangle.createRectangleGraph(rectSet)
    val cf = new BronKerboschCliqueFinder[Rectangle, DefaultEdge](G)

    val cliques = cf.getAllMaximalCliques

    // Return a list of clique sets (sets of rects)
    var scalaCliques = ListBuffer[Set[Rectangle]]()
    val it = cliques.iterator
    while (it.hasNext) {
      scalaCliques += it.next.toSet
    }
    scalaCliques.toList
  }

  override def runFilter(): List[Set[Rectangle]] = getClusters
}
