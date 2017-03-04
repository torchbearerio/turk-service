package io.torchbearer.turkservice.filters

/**
  * Created by fredricvollmer on 1/25/17.
  */
class DescriptorFilter(descriptors: List[Map[String, String]]) extends DescriptionFilterTrait {

  override def runFilter() = {
    // first, we need to build vectors of values for each  key

    List(descriptors.head)
  }
}
