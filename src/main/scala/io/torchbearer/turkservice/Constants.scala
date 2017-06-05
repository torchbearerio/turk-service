package io.torchbearer.turkservice

/**
  * Created by fredricvollmer on 10/30/16.
  */
object Constants {
  // SQS
  final val SQS_HIT_SALIENCY_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/saliency-hits"
  final val SQS_HIT_DESCRIPTION_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/description-hits"
  final val SQS_HIT_VERIFICATION_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/verification-hits"

  // S3
  final val LANDMARK_MARKED_IMAGES_BASE_URL = "https://s3-us-west-2.amazonaws.com/torchbearer-marked-landmark-images"
  final val STREETVIEW_IMAGES_BASE_URL = "https://s3-us-west-2.amazonaws.com/torchbearer-sv-images"

  // Turk Questions
  //final val EXTERNAL_QUESTION_BASE_URL = "https://turkservice.torchbearer.io/question"
  final val EXTERNAL_QUESTION_BASE_URL: String = if (sys.env.getOrElse("ENVIRONMENT", "development") == "development")
    "https://torchbearer.dev/question"
  else
    "https://turkservice.torchbearer.io/question"

  final val EXTERNAL_QUESTION_SUBMIT_URL: String = if (sys.env.getOrElse("TURK_SANDBOX", "false").toBoolean)
      "https://workersandbox.mturk.com/mturk/externalSubmit"
    else
      "https://www.mturk.com/mturk/externalSubmit"

  final val INITIAL_ASSIGNMENT_COUNT = 3
  final val INITIAL_HIT_LIFETIME = 10000

  // Saliency Questions
  final val SALIENCY_INTERNAL_IDENTIFIER = "saliency"
  final val SALIENCY_TITLE = "Image Landmark Selection"
  final val SALIENCY_DESCRIPTION = "Draw a box around the most prominent feature in an image"
  final val SALIENCY_ASSIGNMENT_COUNT = 5
  final val SALIENCY_REWARD = 0.05
  final val SALIENCY_KEYWORDS = "image annotation,image tagging,directions,navigation"

  // Description Questions
  final val DESCRIPTION_INTERNAL_IDENTIFIER = "description"
  final val DESCRIPTION_TITLE = "Image Annotation"
  final val DESCRIPTION_DESCRIPTION = "Describe what's in the image in a few words"
  final val DESCRIPTION_ASSIGNMENT_COUNT = 1
  final val DESCRIPTION_REWARD = 0.10
  final val DESCRIPTION_KEYWORDS = "image annotation,image tagging,directions,navigation"

  // Verification Questions
  final val VERIFICATION_INTERNAL_IDENTIFIER = "verification"
  final val VERIFICATION_TITLE = "Image Annotation Verification"
  final val VERIFICATION_DESCRIPTION = "Verify that the description of an image is accurate"
  final val VERIFICATION_ASSIGNMENT_COUNT = 3
  final val VERIFICATION_REWARD = 0.03
  final val VERIFICATION_KEYWORDS = "image annotation,image tagging,directions,navigation"

  // question versions
  final val OBJECT_DESCRIPTION_QUESTION_VERSION = "1.0"
  final val SALIENCY_QUESTION_VERSION = "1.0"

  // Turk hit types
  final val SALIENCY_HIT_TYPE_ID = "38JF4YFDOJV9QBAKPN4AXTG4SOZ699"
  final val DESCRIPTION_HIT_TYPE_ID = "38ZP8AG6P150VRITKZNR8GEKZGYWI2"

  // filters
  final val FILTER_PACKAGE = "io.torchbearer.turkservice.filters"
}