package io.torchbearer.turkservice

import java.io.File

import com.amazonaws.mturk.service.axis.RequesterService
import com.amazonaws.mturk.service.exception.ServiceException
import com.amazonaws.mturk.util.{ClientConfig, PropertiesClientConfig}
import com.amazonaws.mturk.requester.HIT
import org.apache.commons.io.{FileUtils, FilenameUtils, IOUtils}

object TurkClientFactory {
  private val url = if (sys.env.getOrElse("TURK_SANDBOX", "false").toBoolean)
      "https://mechanicalturk.sandbox.amazonaws.com/?Service=AWSMechanicalTurkRequester"
    else
      "https://mechanicalturk.amazonaws.com/?Service=AWSMechanicalTurkRequester"

  private val propertiesURL = getClass.getResource("/mturk.properties")
  private val propFile = File.createTempFile(
    FilenameUtils.getBaseName(propertiesURL.getFile),
    FilenameUtils.getExtension(propertiesURL.getFile))
  IOUtils.copy(propertiesURL.openStream(),
    FileUtils.openOutputStream(propFile))
  private val cpath = propFile.getCanonicalPath
  private val config = new PropertiesClientConfig(cpath)
  config.setAccessKeyId("AKIAJQ72DSDXNTL746QA")
  config.setSecretAccessKey("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")
  config.setServiceURL(url)

  private val client = new RequesterService(config)

  def getClient = client

}
