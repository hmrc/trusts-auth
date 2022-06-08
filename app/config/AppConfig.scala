/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration,
                          servicesConfig: ServicesConfig) {

  val TAXABLE_ENROLMENT = "HMRC-TERS-ORG"
  val TAXABLE_ENROLMENT_ID = "SAUTR"
  val NON_TAXABLE_ENROLMENT = "HMRC-TERSNT-ORG"
  val NON_TAXABLE_ENROLMENT_ID = "URN"

  val AGENT_ENROLMENT = "HMRC-AS-AGENT"
  val AGENT_ENROLMENT_ID = "AgentReferenceNumber"

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  lazy val unauthorisedUrl: String = config.get[String]("urls.unauthorised")
  lazy val alreadyClaimedUrl: String = config.get[String]("urls.alreadyClaimed")
  lazy val agentNotAuthorisedUrl: String = config.get[String]("urls.agentNotAuthorised")
  lazy val createAgentServicesAccountUrl: String = config.get[String]("urls.createAgentServicesAccount")
  lazy val maintainThisTrust: String = config.get[String]("urls.maintainThisTrust")

  def claimATrustUrl(identifier: String) =
    s"${config.get[String]("urls.startClaimATrust")}/$identifier"

  lazy val relationshipName: String =
    config.get[String]("microservice.services.self.relationship-establishment.name")
  lazy val taxableRelationshipIdentifier: String =
    config.get[String]("microservice.services.self.relationship-establishment.taxable.identifier")
  lazy val nonTaxableRelationshipIdentifier: String =
    config.get[String]("microservice.services.self.relationship-establishment.nonTaxable.identifier")

  lazy val enrolmentStoreProxyUrl: String = servicesConfig.baseUrl("enrolment-store-proxy")

}
