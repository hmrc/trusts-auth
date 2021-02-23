/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import config.AppConfig
import models.{EnrolmentStoreResponse, TrustIdentifier, URN, UTR}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(http: HttpClient, config: AppConfig) extends Logging {

  private def enrolmentsEndpoint(identifier: TrustIdentifier): String = {

    def url(enrolment: String, enrolmentId: String, identifier: String): String = {
      s"${config.enrolmentStoreProxyUrl}/enrolment-store-proxy/enrolment-store/enrolments/" +
        s"$enrolment~$enrolmentId~$identifier/users"
    }

    identifier match {
      case UTR(value) =>
        url(config.TAXABLE_ENROLMENT, config.TAXABLE_ENROLMENT_ID, value)
      case URN(value) =>
        url(config.NON_TAXABLE_ENROLMENT, config.NON_TAXABLE_ENROLMENT_ID, value)
    }
  }

  def checkIfAlreadyClaimed(identifier: TrustIdentifier)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EnrolmentStoreResponse] = {
    val url = enrolmentsEndpoint(identifier)
    logger.info(s"checkIfAlreadyClaimed using $url")
    http.GET[EnrolmentStoreResponse](enrolmentsEndpoint(identifier))(EnrolmentStoreResponse.httpReads, hc, ec)
  }
}
