/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.EnrolmentStoreConnector
import controllers.actions.IdentifierAction
import models.EnrolmentStoreResponse.{AlreadyClaimed, NotClaimed}
import models._
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.{AgentAuthorisedForDelegatedEnrolment, TrustsIV}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrustAuthController @Inject()(val controllerComponents: MessagesControllerComponents,
                                    identifierAction: IdentifierAction,
                                    enrolmentStoreConnector: EnrolmentStoreConnector,
                                    config: AppConfig,
                                    trustsIV: TrustsIV,
                                    delegatedEnrolment: AgentAuthorisedForDelegatedEnrolment
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def authorisedForUtr(utr: String): Action[AnyContent] = identifierAction.async {
    implicit request =>
      implicit val hc : HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)

      mapResult(request.user.affinityGroup match {
        case Agent =>
          checkIfAgentAuthorised(utr)
        case Organisation =>
          checkIfTrustIsClaimedAndTrustIV(utr)
        case _ =>
          Future.successful(TrustAuthDenied(config.unauthorisedUrl))
      })
  }

  def agentAuthorised(): Action[AnyContent] = identifierAction.async {
    implicit request =>
      implicit val hc : HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)

      mapResult(request.user.affinityGroup match {
        case Agent =>
          Future.successful(authoriseAgent(request))
        case Organisation =>
          Future.successful(TrustAuthAllowed)
        case _ =>
          Future.successful(TrustAuthDenied(config.unauthorisedUrl))
      })
  }

  private def mapResult(result: Future[Object]) = result map {
    case TrustAuthInternalServerError => InternalServerError
    case r: TrustAuthResponse => Ok(Json.toJson(r))
  }

  private def authoriseAgent[A](request: IdentifierRequest[A]): TrustAuthResponse = {

    getAgentReferenceNumber(request.user.enrolments) match {
      case Some(arn) if arn.nonEmpty =>
        TrustAuthAgentAllowed(arn)
      case _ =>
        Logger.info(s"[authoriseAgent] not a valid agent service account")
        TrustAuthDenied(config.createAgentServicesAccountUrl)
    }
  }

  private def getAgentReferenceNumber(enrolments: Enrolments) =
    enrolments.enrolments
      .find(_.key equals "HMRC-AS-AGENT")
      .flatMap(_.identifiers.find(_.key equals "AgentReferenceNumber"))
      .collect { case EnrolmentIdentifier(_, value) => value }

  private def checkIfTrustIsClaimedAndTrustIV[A](utr: String)
                                                (implicit request: IdentifierRequest[A],
                                                 hc: HeaderCarrier): Future[TrustAuthResponse] = {

    val userEnrolled = checkForTrustEnrolmentForUTR(utr)

    Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] authenticating user for $utr")

    if (userEnrolled) {
      Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] user is enrolled for $utr")

      trustsIV.authenticate(
        utr = utr,
        onIVRelationshipExisting = {
          Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] user has an IV session for $utr")
          Future.successful(TrustAuthAllowed())
        },
        onIVRelationshipNotExisting = {
          Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] user does not have an IV session for $utr")
          Future.successful(TrustAuthDenied(config.maintainThisTrust))
        }
      )
    } else {
      enrolmentStoreConnector.checkIfAlreadyClaimed(utr) flatMap {
        case AlreadyClaimed =>
          Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] user is not enrolled for $utr and the trust is already claimed")
          Future.successful(TrustAuthDenied(config.alreadyClaimedUrl))

        case NotClaimed =>
          Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] user is not enrolled for $utr and the trust is not claimed")
          Future.successful(TrustAuthDenied(config.claimATrustUrl(utr)))
        case _ =>
          Logger.info(s"[checkIfTrustIsClaimedAndTrustIV] unable to determine if $utr is already claimed")
          Future.successful(TrustAuthInternalServerError)
      }
    }
  }

  private def checkIfAgentAuthorised[A](utr: String)
                                       (implicit request: Request[A],
                                        hc: HeaderCarrier): Future[TrustAuthResponse] = {

    Logger.info(s"[checkIfAgentAuthorised] authenticating agent for $utr")

    enrolmentStoreConnector.checkIfAlreadyClaimed(utr) flatMap {
      case NotClaimed =>
        Logger.info(s"[checkIfAgentAuthorised] agent not authenticated for $utr, trust is not claimed")
        Future.successful(TrustAuthDenied(config.trustNotClaimedUrl))
      case AlreadyClaimed =>
        Logger.info(s"[checkIfAgentAuthorised] $utr is claimed, checking if agent is authorised")
        delegatedEnrolment.authenticate(utr)
      case _ =>
        Logger.info(s"[checkIfAgentAuthorised] unable to determine if $utr is already claimed")
        Future.successful(TrustAuthInternalServerError)
    }
  }

  private def checkForTrustEnrolmentForUTR[A](utr: String)(implicit request: IdentifierRequest[A]): Boolean =
    request.user.enrolments.enrolments
      .find(_.key equals "HMRC-TERS-ORG")
      .flatMap(_.identifiers.find(_.key equals "SAUTR"))
      .exists(_.value equals utr)
}
