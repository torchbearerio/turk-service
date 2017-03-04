package io.torchbearer.turkservice

import com.amazonaws.mturk.requester.{EventType, NotificationSpecification, NotificationTransport, QualificationRequirement}

sealed class Task(val name: String,
                  val id: String,
                  val title: String,
                  val description: String,
                  val reward: Double,
                  val keywords: String,
                  val autoApprovalDelay: Long = 172800,
                  val assingmentDuration: Long = 60,
                  val qualificationRequirements: Array[QualificationRequirement] = Array()) {
  val hitTypeId: String = registerHitType

  def registerHitType: String = {
    val turkClient = TurkClientFactory.getClient
    val t = this
    val notification = new NotificationSpecification("https://sqs.us-west-2.amazonaws.com/814009652816/completed-hits",
      NotificationTransport.SQS, "2006-05-05", Array(EventType.HITReviewable))

    val hitTypeId = turkClient.registerHITType(t.autoApprovalDelay, t.assingmentDuration, t.reward, t.title, t.keywords, t.description, t.qualificationRequirements)
    turkClient.setHITTypeNotification(hitTypeId, notification, true);

    hitTypeId
  }
}

case object SALIENCY_DETECTION extends Task("Saliency Detection",
  "objectsampling",
  "Image Landmark Selection",
  "Draw a box around the most prominent feature in an image",
  0.05,
  "image annotation,image tagging,directions,navigation")

case object OBJECT_DESCRIPTION extends Task("Object Description",
  "objectdescription",
  "Image Description",
  "Provide a few words to describe the pictured landmark",
  0.05,
  "image annotation,image tagging,directions,navigation")