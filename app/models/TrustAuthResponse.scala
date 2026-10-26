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

import play.api.libs.json.{Format, Json, Reads, Writes, __}

sealed trait TrustAuthResponse

object TrustAuthResponse {

  given reads: Reads[TrustAuthResponse] =
    (__ \ "arn").read[String]
      .map[TrustAuthResponse](TrustAuthAgentAllowed.apply)
      .orElse(
        (__ \ "redirectUrl").read[String]
          .map[TrustAuthResponse](TrustAuthDenied.apply)
      )
      .orElse(
        (__ \ "authorised").read[Boolean]
          .map[TrustAuthResponse](TrustAuthAllowed.apply)
      )

  given writes: Writes[TrustAuthResponse] = Writes {
    case r: TrustAuthAllowed          => Json.toJson(r)(using TrustAuthAllowed.format)
    case r: TrustAuthAgentAllowed     => Json.toJson(r)(using TrustAuthAgentAllowed.format)
    case r: TrustAuthDenied           => Json.toJson(r)(using TrustAuthDenied.format)
    case TrustAuthInternalServerError => throw new RuntimeException("Can't write TrustAuthInternalServerError as Json")
  }

}

case class TrustAuthAllowed(authorised: Boolean = true) extends TrustAuthResponse

case object TrustAuthAllowed {
  given format: Format[TrustAuthAllowed] = Json.format[TrustAuthAllowed]
}

case class TrustAuthAgentAllowed(arn: String) extends TrustAuthResponse

case object TrustAuthAgentAllowed {
  given format: Format[TrustAuthAgentAllowed] = Json.format[TrustAuthAgentAllowed]
}

case class TrustAuthDenied(redirectUrl: String) extends TrustAuthResponse

case object TrustAuthDenied {
  given format: Format[TrustAuthDenied] = Json.format[TrustAuthDenied]
}

case object TrustAuthInternalServerError extends TrustAuthResponse
