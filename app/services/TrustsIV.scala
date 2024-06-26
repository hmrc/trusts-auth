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

package services

import com.google.inject.Inject
import controllers.actions.TrustsAuthorisedFunctions
import models.{TrustAuthResponse, TrustIdentifier, URN, UTR}
import play.api.Logging
import uk.gov.hmrc.auth.core.{BusinessKey, FailedRelationship, Relationship}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class TrustsIV @Inject()(trustsAuth: TrustsAuthorisedFunctions) extends Logging {

  def authenticate[A](identifier: TrustIdentifier,
                      onIVRelationshipExisting: => Future[TrustAuthResponse],
                      onIVRelationshipNotExisting: => Future[TrustAuthResponse]
                     )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TrustAuthResponse] = {

    val relationship = identifier match {
      case UTR(utr) =>
        Relationship(trustsAuth.config.relationshipName, Set(BusinessKey(trustsAuth.config.taxableRelationshipIdentifier, utr)))
      case URN(urn) =>
        Relationship(trustsAuth.config.relationshipName, Set(BusinessKey(trustsAuth.config.nonTaxableRelationshipIdentifier, urn)))
    }

    trustsAuth.authorised(relationship) {
      onIVRelationshipExisting
    } recoverWith {
      case FailedRelationship(msg) =>
        logger.info(s"[TrustsIV][authenticate][Session ID: ${Session.id(hc)}][UTR/URN: $identifier]" +
          s" Relationship does not exist in Trust IV for user due to $msg")
        onIVRelationshipNotExisting
    }
  }

}
