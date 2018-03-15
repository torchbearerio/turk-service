package io.torchbearer.turkservice.turkquestions

import io.torchbearer.ServiceCore.Utils._
import io.torchbearer.turkservice.Constants


object TurkQuestionFactory {

  /**
    * Instantiates a saliency question
    * @param epId {Int} The execution point ID for which to crete this saliency question
    * @return A TurkQuestion instance
    */
  def createSaliencyQuestion(epId: Int, hitId: Int): TurkQuestion = {
    val baseUrl = s"${Constants.EXTERNAL_QUESTION_BASE_URL}/${Constants.SALIENCY_INTERNAL_IDENTIFIER}"
    val url = formatURLWithQueryParams(baseUrl,
      "hitId" -> hitId
    )

    val rawQuestionXml =
      <ExternalQuestion xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd">
        <ExternalURL>
          { scala.xml.Unparsed("<![CDATA[%s]]>".format(url)) }
        </ExternalURL>
        <FrameHeight>700</FrameHeight>
      </ExternalQuestion>

    val questionXml = scala.xml.Utility.trim(rawQuestionXml).toString()

    new TurkQuestion(
      Constants.SALIENCY_INTERNAL_IDENTIFIER,
      Constants.SALIENCY_TITLE,
      Constants.SALIENCY_DESCRIPTION,
      Constants.SALIENCY_REWARD,
      Constants.SALIENCY_ASSIGNMENT_COUNT,
      Constants.SALIENCY_KEYWORDS,
      questionXml,
      Constants.SQS_HIT_SALIENCY_URL
    )
  }

  def createDescriptionQuestion(landmarkId: String): TurkQuestion = {
    val landmarkImageUrl = s"${Constants.LANDMARK_MARKED_IMAGES_BASE_URL}/$landmarkId.png"

    val rawQuestionXml =
      <QuestionForm xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd">
        <Overview>
          <Title>
            Describe the outlined area of the image
          </Title>
        </Overview>
        <Question>
          <QuestionIdentifier>description</QuestionIdentifier>
          <IsRequired>true</IsRequired>
          <QuestionContent>
            <Text>
              Provide a detailed description of the object in the RED BOX.
              Pretend you were using that object as a landmark when giving someone directions.
            </Text>
            <EmbeddedBinary>
              <EmbeddedMimeType>
                <Type>image</Type>
                <SubType>png</SubType>
              </EmbeddedMimeType>
              <DataURL>
                { scala.xml.Unparsed("<![CDATA[%s]]>".format(landmarkImageUrl)) }
              </DataURL>
              <AltText>Image showing object to describe</AltText>
              <Width>640</Width>
              <Height>640</Height>
            </EmbeddedBinary>
          </QuestionContent>
          <AnswerSpecification>
            <FreeTextAnswer>
              <Constraints>
                <Length minLength="4" />
              </Constraints>
            </FreeTextAnswer>
          </AnswerSpecification>
        </Question>
      </QuestionForm>

    val questionXml = scala.xml.Utility.trim(rawQuestionXml).toString()

    new TurkQuestion(
      Constants.DESCRIPTION_INTERNAL_IDENTIFIER,
      Constants.DESCRIPTION_TITLE,
      Constants.DESCRIPTION_DESCRIPTION,
      Constants.DESCRIPTION_REWARD,
      //Constants.DESCRIPTION_ASSIGNMENT_COUNT,
      1,
      Constants.DESCRIPTION_KEYWORDS,
      questionXml,
      Constants.SQS_HIT_DESCRIPTION_URL
    )
  }

  /**
    *
    * @param landmarkId
    * @return
    */
  def createDescriptionVerificationQuestion(landmarkId: String, landmarkDescription: String): TurkQuestion = {
    val landmarkImageUrl = s"${Constants.LANDMARK_MARKED_IMAGES_BASE_URL}/$landmarkId.png"

    val rawQuestionXml =
      <QuestionForm xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd">
        <Overview>
          <Title>
            Verify the accuracy of the image description
          </Title>
        </Overview>
        <Question>
          <QuestionIdentifier>vote</QuestionIdentifier>
          <IsRequired>true</IsRequired>
          <QuestionContent>
            <Title>Is the following description accurate for the what's outlined in the RED BOX?</Title>
            <Text>{ s"$landmarkDescription" }</Text>
            <EmbeddedBinary>
              <EmbeddedMimeType>
                <Type>image</Type>
                <SubType>png</SubType>
              </EmbeddedMimeType>
              <DataURL>
                { scala.xml.Unparsed("<![CDATA[%s]]>".format(landmarkImageUrl)) }
              </DataURL>
              <AltText>Image showing object to describe</AltText>
              <Width>640</Width>
              <Height>640</Height>
            </EmbeddedBinary>
          </QuestionContent>
          <AnswerSpecification>
            <SelectionAnswer>
              <StyleSuggestion>radiobutton</StyleSuggestion>
              <Selections>
                <Selection>
                  <SelectionIdentifier>true</SelectionIdentifier>
                  <Text>Yes, it's accurate</Text>
                </Selection>
                <Selection>
                  <SelectionIdentifier>false</SelectionIdentifier>
                  <Text>No, it's NOT accurate</Text>
                </Selection>
              </Selections>
            </SelectionAnswer>
          </AnswerSpecification>
        </Question>
      </QuestionForm>

    val questionXml = scala.xml.Utility.trim(rawQuestionXml).toString()

    new TurkQuestion(
      Constants.VERIFICATION_INTERNAL_IDENTIFIER,
      Constants.VERIFICATION_TITLE,
      Constants.VERIFICATION_DESCRIPTION,
      Constants.VERIFICATION_REWARD,
      //Constants.VERIFICATION_ASSIGNMENT_COUNT,
      1,
      Constants.VERIFICATION_KEYWORDS,
      questionXml,
      Constants.SQS_HIT_VERIFICATION_URL
    )
  }
}
