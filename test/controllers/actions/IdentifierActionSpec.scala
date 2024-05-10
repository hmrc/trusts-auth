/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import base.SpecBase
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{AgentInformation, Retrieval, ~}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentifierActionSpec extends SpecBase {

  type RetrievalType = Option[String] ~ Option[AffinityGroup] ~ Enrolments

  val mockAuthConnector: AuthConnector = Mockito.mock(classOf[AuthConnector])
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  lazy val trustsAuth = new TrustsAuthorisedFunctions(mockAuthConnector, appConfig)

  private val noEnrollment = Enrolments(Set())

  val agentInformation: AgentInformation = AgentInformation(None, None, None)

  private def fakeRequest = FakeRequest("", "")

  private def authRetrievals(affinityGroup: AffinityGroup,
                             enrolment: Enrolments,
                             agentInformation: AgentInformation): Future[Some[String] ~ Some[AffinityGroup] ~ Enrolments] =
    Future.successful(new~(new~(Some("id"), Some(affinityGroup)), enrolment))

  private val agentEnrolment = Enrolments(Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReferenceNumber", "SomeVal")), "Activated", None)))

  private val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

  protected def applicationBuilder(affinityGroup: AffinityGroup = AffinityGroup.Organisation,
                                   enrolments: Enrolments = Enrolments(Set.empty[Enrolment])
                                  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers, affinityGroup, enrolments))
      )
      .configure(defaultAppConfigurations)

  "invoking an AuthenticatedIdentifier" when {

    "Agent user has correct enrolled in Agent Services Account" must {
      "allow user to continue" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Agent, agentEnrolment, agentInformation))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
        application.stop()
      }
    }

    "Org user with no enrolments" must {
      "allow user to continue" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation, agentEnrolment, agentInformation))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
        application.stop()
      }
    }

    "Individual user" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Individual, noEnrollment, agentInformation))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK

        application.stop()
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed MissingBearerToken())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed BearerTokenExpired())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed InsufficientEnrolments())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user doesn't have sufficient confidence level" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed InsufficientConfidenceLevel())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user used an unaccepted auth provider" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed UnsupportedAuthProvider())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user has an unsupported affinity group" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed UnsupportedAffinityGroup())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }

    "the user has an unsupported credential role" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder().build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())) thenReturn (Future failed UnsupportedCredentialRole())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)
        val controller = new Harness(action)
        val result = controller.onPageLoad()(fakeRequest)

        status(result) mustBe UNAUTHORIZED

        application.stop()
      }
    }
  }
}

