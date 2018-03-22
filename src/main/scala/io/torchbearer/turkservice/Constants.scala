package io.torchbearer.turkservice

/**
  * Created by fredricvollmer on 10/30/16.
  */
object Constants {
  // SQS
  val SQS_HIT_SALIENCY_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/saliency-hits"
  val SQS_HIT_DESCRIPTION_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/description-hits"
  val SQS_HIT_VERIFICATION_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/verification-hits"

  // S3
  val LANDMARK_MARKED_IMAGES_BASE_URL = "https://s3-us-west-2.amazonaws.com/torchbearer-marked-landmark-images"
  val STREETVIEW_IMAGES_BASE_URL = "https://s3-us-west-2.amazonaws.com/torchbearer-sv-images"

  // Turk Questions
  val EXTERNAL_QUESTION_BASE_URL: String = if (sys.env.getOrElse("ENVIRONMENT", "production") == "development")
   // "https://torchbearer.dev/question"
    "https://f4c3cbf9.ngrok.io/question"
  else
    "https://turkservice.torchbearer.io/question"

  val EXTERNAL_QUESTION_SUBMIT_URL: String = if (sys.env.getOrElse("TURK_SANDBOX", "false").toBoolean)
    "https://workersandbox.mturk.com/mturk/externalSubmit"
  else
    "https://www.mturk.com/mturk/externalSubmit"

  val INITIAL_ASSIGNMENT_COUNT = 3
  val INITIAL_HIT_LIFETIME = 10000

  val MAX_DESCRIPTION_ATTEMPTS = 3

  // Saliency Questions
  val SALIENCY_INTERNAL_IDENTIFIER = "saliency"
  val SALIENCY_TITLE = "Landmark selection: draw boxes around landmarks in 3 images"
  val SALIENCY_DESCRIPTION = "Draw a box around the most prominent feature in 3 images"
  val SALIENCY_ASSIGNMENT_COUNT = 1
  val SALIENCY_REWARD = 0.05
  val SALIENCY_KEYWORDS = "image annotation,image tagging,directions,navigation,object detection,draw"
  val SALIENCY_DURATION: Int = 10 * 60

  // Description Questions
  val DESCRIPTION_INTERNAL_IDENTIFIER = "description"
  val DESCRIPTION_TITLE = "Image annotation: describe the landmark shown in the image"
  val DESCRIPTION_DESCRIPTION = "Describe the outlined landmark in a few words"
  val DESCRIPTION_ASSIGNMENT_COUNT = 1
  val DESCRIPTION_REWARD = 0.10
  val DESCRIPTION_KEYWORDS = "image annotation,image tagging,directions,navigation"
  val DESCRIPTION_DURATION: Int = 10 * 60

  // Verification Questions
  val VERIFICATION_INTERNAL_IDENTIFIER = "verification"
  val VERIFICATION_TITLE = "Landmark Image Description Verification"
  val VERIFICATION_DESCRIPTION = "Decide whether the description of an image is accurate"
  val VERIFICATION_ASSIGNMENT_COUNT = 1
  val VERIFICATION_REWARD = 0.03
  val VERIFICATION_KEYWORDS = "image annotation,image tagging,directions,navigation"
  val VERIFICATION_DURATION: Int = 3 * 60

  // question versions
  val OBJECT_DESCRIPTION_QUESTION_VERSION = "1.0"
  val SALIENCY_QUESTION_VERSION = "1.0"

  // filters
  val FILTER_PACKAGE = "io.torchbearer.turkservice.filters"
}