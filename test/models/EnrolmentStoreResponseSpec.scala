/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import base.SpecBase
import models.EnrolmentStoreResponse.{httpReads, *}
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class EnrolmentStoreResponseSpec extends SpecBase {

  private val url = "/enrolment-store-proxy/enrolment-store/enrolments"

  private def readResponse(
                            status: Int,
                            body: String = ""
                          ): EnrolmentStoreResponse =
    httpReads.read(
      "GET",
      url,
      HttpResponse(status, body)
    )

  private def enrolmentStoreJson(
                                  principalUserIds: Seq[String],
                                  delegatedUserIds: Seq[String]
                                ): String =
    Json.stringify(
      Json.obj(
        "principalUserIds" -> principalUserIds,
        "delegatedUserIds" -> delegatedUserIds
      )
    )

  "EnrolmentStoreResponse" when {

    "HTTP status is 200 OK" should {

      "return NotClaimed when principalUserIds and delegatedUserIds are empty" in {
        val body = enrolmentStoreJson(
          principalUserIds = Seq.empty,
          delegatedUserIds = Seq.empty
        )

        readResponse(OK, body) mustBe NotClaimed
      }

      "return NotClaimed when principalUserIds is empty but delegatedUserIds is non-empty" in {
        val body = enrolmentStoreJson(
          principalUserIds = Seq.empty,
          delegatedUserIds = Seq("delegatedUser1")
        )

        readResponse(OK, body) mustBe NotClaimed
      }

      "return AlreadyClaimed when principalUserIds is non-empty" in {
        val body = enrolmentStoreJson(
          principalUserIds = Seq("principalUser1"),
          delegatedUserIds = Seq.empty
        )

        readResponse(OK, body) mustBe AlreadyClaimed
      }

      "return AlreadyClaimed when both principalUserIds and delegatedUserIds are non-empty" in {
        val body = enrolmentStoreJson(
          principalUserIds = Seq("principalUser1"),
          delegatedUserIds = Seq("delegatedUser1")
        )

        readResponse(OK, body) mustBe AlreadyClaimed
      }
    }

    "HTTP status is 204 No Content" should {

      "return NotClaimed" in {
        readResponse(NO_CONTENT) mustBe NotClaimed
      }
    }

    "HTTP status is 503 Service Unavailable" should {

      "return ServiceUnavailable" in {
        readResponse(SERVICE_UNAVAILABLE) mustBe ServiceUnavailable
      }
    }

    "HTTP status is 403 Forbidden" should {

      "return Forbidden" in {
        readResponse(FORBIDDEN) mustBe Forbidden
      }
    }

    "HTTP status is 400 Bad Request" should {

      "return BadRequest" in {
        readResponse(BAD_REQUEST) mustBe BadRequest
      }
    }

    "HTTP status is unexpected" should {

      "return ServerError for 404 Not Found" in {
        readResponse(NOT_FOUND) mustBe ServerError
      }

      "return ServerError for 500 Internal Server Error" in {
        readResponse(INTERNAL_SERVER_ERROR) mustBe ServerError
      }

      "return ServerError for 502 Bad Gateway" in {
        readResponse(BAD_GATEWAY) mustBe ServerError
      }
    }

    "serialising and deserialising EnrolmentStore JSON" should {

      "preserve principalUserIds and delegatedUserIds" in {
        val enrolmentStore = EnrolmentStore(
          principalUserIds = Seq("principalUser1"),
          delegatedUserIds = Seq("delegatedUser1")
        )

        val json = Json.toJson(enrolmentStore)(using EnrolmentStoreResponse.format)

        json.as[EnrolmentStore](using EnrolmentStoreResponse.format).mustBe(enrolmentStore)
      }
    }
  }
}
