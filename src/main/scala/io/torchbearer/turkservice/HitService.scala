package io.torchbearer.turkservice

import com.amazonaws.services.cloudfront.model.InvalidArgumentException
import io.torchbearer.ServiceCore.DataModel.{ExecutionPoint, Hit}
import io.torchbearer.ServiceCore.Utils._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Created by fredricvollmer on 11/11/16.
  */
object HitService {
  private val turkClient = TurkClientFactory.getClient

  private def getHitReward(t: Task, hit: Hit): Double = {
    t match {
      case SALIENCY_DETECTION => hit.saliencyReward / 100.0
      case OBJECT_DESCRIPTION => hit.descriptionReward / 100.0
      case _ => 0.05
    }
  }

  private def getAssignmentCount(t: Task, hit: Hit): Int = {
    t match {
      case SALIENCY_DETECTION => hit.saliencyAssignemntCount
      case OBJECT_DESCRIPTION => hit.descriptionAssignemntCount
      case _ => 10
    }
  }

  private def submitHitForExecutionPoint(epId: Int, t: Task, assignmentCount: Int,
                                         reward: Double, lifetime: Long): String = {
    val baseUrl = s"${Constants.EXTERNAL_QUESTION_BASE_URL}/${t.id}"
    val url = formatURLWithQueryParams(baseUrl,
      "epId" -> epId
      //"lat" -> ep.lat,
      //"long" -> ep.long,
      //"bearing" -> ep.bearing
      //"saliencyHitId" -> saliencyHitId.getOrElse("0")
    )

    val questionXML =
      <ExternalQuestion xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd">
        <ExternalURL>
          { scala.xml.Unparsed("<![CDATA[%s]]>".format(url)) }
        </ExternalURL>
        <FrameHeight>700</FrameHeight>
      </ExternalQuestion>
    val question = scala.xml.Utility.trim(questionXML).toString()
    val hit = turkClient.createHIT(null, t.title, t.description, t.keywords, question, reward, t.assingmentDuration, t.autoApprovalDelay, lifetime, assignmentCount, null, t.qualificationRequirements, null)

    println(s"Created ${t.name} hit ${hit.getHITId})")

    hit.getHITId
  }

  def initiateHit(hit: Hit): Unit = {
    println("HitService: processing hit...")
    val reward = getHitReward(SALIENCY_DETECTION, hit)
    val assignmentCount = getAssignmentCount(SALIENCY_DETECTION, hit)
    val saliencyHitId = submitHitForExecutionPoint(hit.executionPoint, SALIENCY_DETECTION, assignmentCount,
      reward, Constants.INITIAL_HIT_LIFETIME)

    // Save this hit to database
    hit.saliencyHitId = Some(saliencyHitId)
    hit.status = s"IN PROGRESS: ${SALIENCY_DETECTION.name}"
    Hit.insertHit(hit)

    println(s"Processing complete, hitId: $saliencyHitId")
  }

  def runNextHitType(hit: Hit): Unit = {
    val reward = getHitReward(OBJECT_DESCRIPTION, hit)
    val assignmentCount = getAssignmentCount(OBJECT_DESCRIPTION, hit)

    val descriptionHitId = submitHitForExecutionPoint(hit.executionPoint, OBJECT_DESCRIPTION, assignmentCount,
      reward, Constants.INITIAL_HIT_LIFETIME)

    // Update hit to point to this enw description hit
    hit.updateStatus(s"IN PROGRESS: ${SALIENCY_DETECTION.name}")
    hit.updateDescriptionHitId(descriptionHitId)
  }
}
