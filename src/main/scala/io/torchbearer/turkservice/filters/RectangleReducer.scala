package io.torchbearer.turkservice.filters

import io.torchbearer.ServiceCore.tyoes.Rectangle

/**
  * Created by fredricvollmer on 11/3/16.
  */
trait RectangleReducer {
  def reduce(): Rectangle
}

class AverageRectangleReducer(rects: List[Rectangle]) extends RectangleReducer with SaliencyFilterTrait {
  def reduce(): Rectangle = {
    // Sum rectangle measurements
    val rect = rects.foldLeft(Rectangle(0, 0, 0, 0))((result, rect) => {
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
  }

  def runFilter(): List[Rectangle] = List(reduce())
}
