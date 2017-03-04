package io.torchbearer.turkservice.nlp

import io.torchbearer.turkservice.forms.FormQuestion

/**
  * Created by fredricvollmer on 1/11/17.
  */
object QueryBuilder {
  def getNextQuestion(data: List[DescriptorResult]): (String, Any) = {
    // If description is done
    if (data.indexWhere(_.name == "description") >= 0) {
      return "status" -> "complete"
    }

    val noun = data.find(_.name == "noun") getOrElse DescriptorResult("noun", "landmark")

    "status" -> "continue"
    "form" -> Array(
      FormQuestion("text", "description", "Description", "How would you describe it?",
        s"Please enter words which describe the ${noun.value}. For example, if this was a brick house, you could type large red brick")
    )
  }
}
