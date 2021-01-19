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

package services

import com.google.inject.Inject
import config.AppConfig
import controllers.actions.TrustsAuthorisedFunctions
import models.{TrustAuthAllowed, TrustAuthDenied, TrustAuthResponse}
import play.api.Logger
import uk.gov.hmrc.auth.core.{Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class AgentAuthorisedForDelegatedEnrolment @Inject()(trustsAuth: TrustsAuthorisedFunctions, config: AppConfig) {

  private val logger: Logger = Logger(getClass)

  def authenticate[A](utr: String)
                     (implicit hc: HeaderCarrier,
                      ec: ExecutionContext): Future[TrustAuthResponse] = {

    val predicate = Enrolment("HMRC-TERS-ORG")
      .withIdentifier("SAUTR", utr)
      .withDelegatedAuthRule("trust-auth")

    trustsAuth.authorised(predicate) {
      logger.info(s"[Session ID: ${Session.id(hc)}] agent is authorised for delegated enrolment for $utr")
      Future.successful(TrustAuthAllowed())
    } recover {
      case _ : InsufficientEnrolments =>
        logger.info(s"[Session ID: ${Session.id(hc)}] agent is not authorised for delegated enrolment for $utr")
        TrustAuthDenied(config.agentNotAuthorisedUrl)
      case _ =>
        logger.info(s"[Session ID: ${Session.id(hc)}] agent is not authorised for $utr")
        TrustAuthDenied(config.unauthorisedUrl)
    }
  }
}
