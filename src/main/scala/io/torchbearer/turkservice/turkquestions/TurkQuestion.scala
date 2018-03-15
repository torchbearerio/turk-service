package io.torchbearer.turkservice.turkquestions

import com.amazonaws.services.mturk.model.{CreateHITRequest, QualificationRequirement}
import io.torchbearer.turkservice.Constants
import io.torchbearer.ServiceCore.AWSServices.MechTurk
import io.torchbearer.ServiceCore.AWSServices.MechTurk._
import collection.JavaConverters._

sealed class TurkQuestion(val internalIdentifier: String,
                          val title: String,
                          val description: String,
                          val reward: Double,
                          val assignmentCount: Int,
                          val keywords: String,
                          var questionXml: String,
                          var sqsUrl: String,
                          val autoApprovalDelay: Long = 172800,
                          val assingmentDuration: Long = 120,
                          val hitDuration: Long = Constants.INITIAL_HIT_LIFETIME,
                          val qualificationRequirements: List[QualificationRequirement] = List()) {
  //val hitTypeId: String = registerHitType
  var mechTurkHitId = ""

  /*
  def registerHitType: String = {
    val turkClient = MechTurk.getClient
    val t = this
    val notification = new NotificationSpecification("https://sqs.us-west-2.amazonaws.com/814009652816/completed-hits",
      NotificationTransport.SQS, "2006-05-05", Array(EventType.HITReviewable))

    val hitTypeId = turkClient.registerHITType(t.autoApprovalDelay, t.assingmentDuration, t.reward, t.title, t.keywords, t.description, t.qualificationRequirements)
    turkClient.setHITTypeNotification(hitTypeId, notification, true);

    hitTypeId
  }
  */

  def submit(): Unit = {
    val turkClient = MechTurk.getClient

    val req = new CreateHITRequest()
    req.setTitle(this.title)
    req.setDescription(this.description)
    req.setKeywords(this.keywords)
    req.setQuestion(this.questionXml)
    req.setReward(s"${this.reward}")
    req.setAssignmentDurationInSeconds(this.assingmentDuration)
    req.setAutoApprovalDelayInSeconds(this.autoApprovalDelay)
    req.setLifetimeInSeconds(this.hitDuration)
    req.setMaxAssignments(this.assignmentCount)
    req.setQualificationRequirements(this.qualificationRequirements.asJava)

    val hit = turkClient.createHIT(req)

    this.mechTurkHitId = hit.getHIT.getHITId

    turkClient.sendNotificationsToQueue(hit.getHIT.getHITTypeId, sqsUrl)

    println(s"Created ${this.internalIdentifier} hit $mechTurkHitId)")
  }
}