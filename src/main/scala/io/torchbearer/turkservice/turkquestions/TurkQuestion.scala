package io.torchbearer.turkservice.turkquestions

import com.amazonaws.mturk.requester.{EventType, NotificationSpecification, NotificationTransport, QualificationRequirement}
import io.torchbearer.turkservice.{Constants, TurkClientFactory}

sealed class TurkQuestion(val internalIdentifier: String,
                          val title: String,
                          val description: String,
                          val reward: Double,
                          val assignmentCount: Int,
                          val keywords: String,
                          var questionXml: String,
                          val autoApprovalDelay: Long = 172800,
                          val assingmentDuration: Long = 120,
                          val hitDuration: Long = Constants.INITIAL_HIT_LIFETIME,
                          val qualificationRequirements: Array[QualificationRequirement] = Array()) {
  //val hitTypeId: String = registerHitType
  var mechTurkHitId = ""

  def registerHitType: String = {
    val turkClient = TurkClientFactory.getClient
    val t = this
    val notification = new NotificationSpecification("https://sqs.us-west-2.amazonaws.com/814009652816/completed-hits",
      NotificationTransport.SQS, "2006-05-05", Array(EventType.HITReviewable))

    val hitTypeId = turkClient.registerHITType(t.autoApprovalDelay, t.assingmentDuration, t.reward, t.title, t.keywords, t.description, t.qualificationRequirements)
    turkClient.setHITTypeNotification(hitTypeId, notification, true);

    hitTypeId
  }

  def submit(): Unit = {
    val turkClient = TurkClientFactory.getClient

    val hit = turkClient.createHIT(null, this.title, this.description, this.keywords, this.questionXml, this.reward,
      this.assingmentDuration, this.autoApprovalDelay, this.hitDuration, this.assignmentCount, null,
      this.qualificationRequirements, null)

    this.mechTurkHitId = hit.getHITId

    println(s"Created ${this.internalIdentifier} hit ${hit.getHITId})")
  }
}