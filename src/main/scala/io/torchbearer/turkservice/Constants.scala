package io.torchbearer.turkservice

/**
  * Created by fredricvollmer on 10/30/16.
  */
object Constants {
  // SQS
  final val SQS_HIT_COMPLETION_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/completed-hits"
  final val SQS_PROCESSING_URL = "https://sqs.us-west-2.amazonaws.com/814009652816/hit-service_processing"

  // Turk Questions
  final val EXTERNAL_QUESTION_BASE_URL = "https://turkservice.torchbearer.io/question"
  final val INITIAL_ASSIGNMENT_COUNT = 3
  final val INITIAL_HIT_LIFETIME = 10000

  // question versions
  final val OBJECT_DESCRIPTION_QUESTION_VERSION = "1.0"
  final val SALIENCY_QUESTION_VERSION = "1.0"

  // Turk hit types
  final val SALIENCY_HIT_TYPE_ID = "38JF4YFDOJV9QBAKPN4AXTG4SOZ699"
  final val DESCRIPTION_HIT_TYPE_ID = "38ZP8AG6P150VRITKZNR8GEKZGYWI2"

  // filters
  final val FILTER_PACKAGE = "io.torchbearer.turkservice.filters"
}