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

package controllers

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.EnrolmentStoreConnector
import controllers.actions.IdentifierAction
import models.EnrolmentStoreResponse.{AlreadyClaimed, NotClaimed}
import models._
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import services.{AgentAuthorisedForDelegatedEnrolment, TrustsIV}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrustAuthController @Inject()(cc: ControllerComponents,
                                    identifierAction: IdentifierAction,
                                    enrolmentStoreConnector: EnrolmentStoreConnector,
                                    config: AppConfig,
                                    trustsIV: TrustsIV,
                                    delegatedEnrolment: AgentAuthorisedForDelegatedEnrolment
                                   )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def authorisedForIdentifier(identifier: String): Action[AnyContent] = identifierAction.async {
    implicit request =>
      implicit val hc : HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)

      mapResult(request.user.affinityGroup match {
        case Agent =>
          checkIfAgentAuthorised(TrustIdentifier(identifier))
        case Organisation =>
          checkIfTrustIsClaimedAndTrustIV(TrustIdentifier(identifier))
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

  private def mapResult(result: Future[Object]): Future[Result] = result map {
    case TrustAuthInternalServerError => InternalServerError
    case r: TrustAuthResponse => Ok(Json.toJson(r))
  }

  private def authoriseAgent[A](request: IdentifierRequest[A])(implicit hc: HeaderCarrier): TrustAuthResponse = {

    getAgentReferenceNumber(request.user.enrolments) match {
      case Some(arn) if arn.nonEmpty =>
        TrustAuthAgentAllowed(arn)
      case _ =>
        logger.info(s"[authoriseAgent][Session ID: ${Session.id(hc)}] not a valid agent service account")
        TrustAuthDenied(config.createAgentServicesAccountUrl)
    }
  }

  private def getAgentReferenceNumber(enrolments: Enrolments): Option[String] =
    enrolments.enrolments
      .find(_.key equals "HMRC-AS-AGENT")
      .flatMap(_.identifiers.find(_.key equals "AgentReferenceNumber"))
      .collect { case EnrolmentIdentifier(_, value) => value }


  private def checkIfTrustIsClaimedAndTrustIV[A](identifier: TrustIdentifier)
                                                (implicit request: IdentifierRequest[A], hc: HeaderCarrier): Future[TrustAuthResponse] = {

    val userEnrolled = checkForTrustEnrolmentForIdentifier(identifier)

    logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
      s" authenticating user for ${identifier.value}")

    if (userEnrolled) {
      logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
        s" user is enrolled for ${identifier.value}")

      trustsIV.authenticate(
        utr = identifier.value,
        onIVRelationshipExisting = {
          logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
            s" user has an IV session for ${identifier.value}")
          Future.successful(TrustAuthAllowed())
        },
        onIVRelationshipNotExisting = {
          logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
            s" user does not have an IV session for ${identifier.value}")
          Future.successful(TrustAuthDenied(config.maintainThisTrust))
        }
      )
    } else {
      enrolmentStoreConnector.checkIfAlreadyClaimed(identifier) flatMap {
        case AlreadyClaimed =>
          logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
            s" user is not enrolled for ${identifier.value} and the trust is already claimed")
          Future.successful(TrustAuthDenied(config.alreadyClaimedUrl))

        case NotClaimed =>
          logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
            s" user is not enrolled for ${identifier.value} and the trust is not claimed")
          Future.successful(TrustAuthDenied(config.claimATrustUrl(identifier.value)))
        case _ =>
          logger.info(s"[checkIfTrustIsClaimedAndTrustIV][Session ID: ${Session.id(hc)}]" +
            s" unable to determine if ${identifier.value} is already claimed")
          Future.successful(TrustAuthInternalServerError)
      }
    }
  }

  private def checkIfAgentAuthorised[A](identifier: TrustIdentifier)(implicit hc: HeaderCarrier): Future[TrustAuthResponse] = {

    logger.info(s"[checkIfAgentAuthorised][Session ID: ${Session.id(hc)}] authenticating agent for ${identifier.value}")

    enrolmentStoreConnector.checkIfAlreadyClaimed(identifier) flatMap {
      case NotClaimed =>
        logger.info(s"[checkIfAgentAuthorised][Session ID: ${Session.id(hc)}] agent not authenticated for ${identifier.value}, trust is not claimed")
        Future.successful(TrustAuthDenied(config.trustNotClaimedUrl))
      case AlreadyClaimed =>
        logger.info(s"[checkIfAgentAuthorised][Session ID: ${Session.id(hc)}] ${identifier.value} is claimed, checking if agent is authorised")
        delegatedEnrolment.authenticate(identifier)
      case _ =>
        logger.info(s"[checkIfAgentAuthorised][Session ID: ${Session.id(hc)}] unable to determine if ${identifier.value} is already claimed")
        Future.successful(TrustAuthInternalServerError)
    }
  }

  private def checkForTrustEnrolmentForIdentifier[A](identifier: TrustIdentifier)(implicit request: IdentifierRequest[A]): Boolean = {
    identifier match {
      case UTR(value) =>
        request.user.enrolments.enrolments
          .find(_.key equals config.TAXABLE_ENROLMENT)
          .flatMap(_.identifiers.find(_.key equals config.TAXABLE_ENROLMENT_ID))
          .exists(_.value equals value)
      case URN(value) =>
        request.user.enrolments.enrolments
          .find(_.key equals config.NON_TAXABLE_ENROLMENT)
          .flatMap(_.identifiers.find(_.key equals config.NON_TAXABLE_ENROLMENT_ID))
          .exists(_.value equals value)
    }
  }
}
