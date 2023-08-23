/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.EnrolmentStoreResponse.{AlreadyClaimed, BadRequest, Forbidden, NotClaimed, ServiceUnavailable}
import models.{URN, UTR}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class EnrolmentStoreConnectorSpec extends AsyncFreeSpec with Matchers with WireMockHelper {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private def wiremock(expectedStatus: Int, expectedResponse: Option[String]): StubMapping = {

    val response = expectedResponse map { response =>
      aResponse()
        .withStatus(expectedStatus)
        .withBody(response)
    } getOrElse {
      aResponse()
        .withStatus(expectedStatus)
    }

    server.stubFor(get(urlEqualTo(utrEnrolmentsUrl)).willReturn(response))
    server.stubFor(get(urlEqualTo(urnEnrolmentsUrl)).willReturn(response))
  }

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Seq(
      "microservice.services.enrolment-store-proxy.port" -> server.port(),
      "auditing.enabled" -> false
    ): _*).build()

  private lazy val connector = app.injector.instanceOf[EnrolmentStoreConnector]

  private lazy val utrServiceName = "HMRC-TERS-ORG"
  private val utrIdentifierKey = "SAUTR"
  private val utrIdentifier = UTR("0987654321")
  private lazy val utrEnrolmentsUrl: String = s"/enrolment-store-proxy/enrolment-store/enrolments/" +
    s"$utrServiceName~$utrIdentifierKey~${utrIdentifier.value}/users"

  private lazy val urnServiceName = "HMRC-TERSNT-ORG"
  private val urnIdentifierKey = "URN"
  private val urnIdentifier = URN("XATRUST12345678")
  private lazy val urnEnrolmentsUrl: String = s"/enrolment-store-proxy/enrolment-store/enrolments/" +
    s"$urnServiceName~$urnIdentifierKey~${urnIdentifier.value}/users"

  private val principalId = Seq("ABCEDEFGI1234567")

  "EnrolmentStoreConnector" - {

    "checkIfAlreadyClaimed" - {
      "No Content when" - {
        "No Content 204" in {

          wiremock(
            expectedStatus = Status.NO_CONTENT,
            expectedResponse = None
          )

          connector.checkIfAlreadyClaimed(utrIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(utrEnrolmentsUrl)))
            result mustBe NotClaimed
          }

        }
      }

      "Cannot access trust when" - {
        "non-empty principalUserIds retrieved using utr" in {

          wiremock(
            expectedStatus = Status.OK,
            expectedResponse = Some(
              s"""{
                 |    "principalUserIds": [
                 |       "${principalId.head}"
                 |    ],
                 |    "delegatedUserIds": [
                 |    ]
                 |}""".stripMargin
            ))

          connector.checkIfAlreadyClaimed(utrIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(utrEnrolmentsUrl)))
            result mustBe AlreadyClaimed
          }

        }

        "non-empty principalUserIds retrieved using urn" in {

          wiremock(
            expectedStatus = Status.OK,
            expectedResponse = Some(
              s"""{
                 |    "principalUserIds": [
                 |       "${principalId.head}"
                 |    ],
                 |    "delegatedUserIds": [
                 |    ]
                 |}""".stripMargin
            ))

          connector.checkIfAlreadyClaimed(urnIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(urnEnrolmentsUrl)))
            result mustBe AlreadyClaimed
          }

        }
      }

      "Service Unavailable when" - {
        "Service Unavailable 503" in {

          wiremock(
            expectedStatus = Status.SERVICE_UNAVAILABLE,
            expectedResponse = Some(
              """
                |{
                |   "errorCode": "SERVICE_UNAVAILABLE",
                |   "message": "Service temporarily unavailable"
                |}""".stripMargin
            ))

          connector.checkIfAlreadyClaimed(utrIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(utrEnrolmentsUrl)))
            result mustBe ServiceUnavailable
          }

        }
      }

      "Forbidden when" - {
        "Forbidden 403" in {

          wiremock(
            expectedStatus = Status.FORBIDDEN,
            expectedResponse = Some(
              """
                |{
                |   "errorCode": "CREDENTIAL_CANNOT_PERFORM_ADMIN_ACTION",
                |   "message": "The User credentials are valid but the user does not have permission to perform the requested function"
                |}""".stripMargin
            ))

          connector.checkIfAlreadyClaimed(utrIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(utrEnrolmentsUrl)))
            result mustBe Forbidden
          }

        }
      }

      "Invalid service when" - {
        "Bad Request 400" in {

          wiremock(
            expectedStatus = Status.BAD_REQUEST,
            expectedResponse = Some(
              """
                |{
                |   "errorCode": "INVALID_SERVICE",
                |   "message": "The provided service does not exist"
                |}""".stripMargin
            ))

          connector.checkIfAlreadyClaimed(utrIdentifier) map { result =>
            server.verify(getRequestedFor(urlEqualTo(utrEnrolmentsUrl)))
            result mustBe BadRequest
          }

        }
      }
    }
  }

}
