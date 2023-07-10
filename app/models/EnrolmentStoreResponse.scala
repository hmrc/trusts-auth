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

package models

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

sealed trait EnrolmentStoreResponse

object EnrolmentStoreResponse extends Logging {

  implicit val format: Format[EnrolmentStore] = Json.format[EnrolmentStore]

  case class EnrolmentStore(principalUserIds: Seq[String], delegatedUserIds: Seq[String]) extends EnrolmentStoreResponse

  case object NotClaimed extends EnrolmentStoreResponse

  case object ServiceUnavailable extends EnrolmentStoreResponse

  case object Forbidden extends EnrolmentStoreResponse

  case object BadRequest extends EnrolmentStoreResponse

  case object ServerError extends EnrolmentStoreResponse

  case object AlreadyClaimed extends EnrolmentStoreResponse

  implicit lazy val httpReads: HttpReads[EnrolmentStoreResponse] =
    (method: String, url: String, response: HttpResponse) => {
      logger.debug(s"[EnrolmentStoreResponse] Response status received from ES0 api: ${response.status}")

      response.status match {
        case OK =>
          response.json.as[EnrolmentStore] match {
            case EnrolmentStore(Seq(), _) =>
              logger.info("[EnrolmentStoreResponse] UTR has not been claimed")
              NotClaimed
            case _ =>
              logger.info("[EnrolmentStoreResponse] UTR has already been claimed")
              AlreadyClaimed
          }
        case NO_CONTENT =>
          logger.info("[EnrolmentStoreResponse] UTR is not claimed or delegated")
          NotClaimed
        case SERVICE_UNAVAILABLE =>
          ServiceUnavailable
        case FORBIDDEN =>
          Forbidden
        case BAD_REQUEST =>
          BadRequest
        case _ =>
          logger.info("[EnrolmentStoreResponse] Unexpected response from EnrolmentStore")
          ServerError
      }
    }
}
