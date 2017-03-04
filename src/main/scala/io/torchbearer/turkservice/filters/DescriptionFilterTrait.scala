package io.torchbearer.turkservice.filters

/**
  * Created by fredvollmer on 1/22/17.
  */

trait DescriptionFilterTrait {
  def runFilter(): List[Map[String, String]]
}
