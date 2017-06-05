package io.torchbearer.turkservice.filters

import io.torchbearer.ServiceCore.tyoes.Rectangle

/**
  * Created by fredricvollmer on 11/3/16.
  */
trait RectangleReducer {
  def reduce(): Set[Rectangle]
}

class AverageRectangleReducer(rects: List[Set[Rectangle]]) extends RectangleReducer with SaliencyFilterTrait {
  def reduce(): Set[Rectangle] = {
    // Reduce each cluster set down to a single rect
    rects.map(clusterSet => {
      // Sum rectangle measurements
      val rect = clusterSet.foldLeft(Rectangle(0, 0, 0, 0))((result, rect) => {
        Rectangle(
          result.x1 + rect.x1,
          result.x2 + rect.x2,
          result.y1 + rect.y1,
          result.y2 + rect.y2
        )
      })

      // Take average
      Rectangle(
        rect.x1 / rects.length,
        rect.x2 / rects.length,
        rect.y1 / rects.length,
        rect.y2 / rects.length
      )
    }).toSet
  }

  def runFilter(): List[Set[Rectangle]] = List(reduce())
}
