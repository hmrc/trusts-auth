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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val authBaseUrl: String = config.get[Service]("microservice.services.auth").baseUrl

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

  lazy val loginUrl: String = config.get[String]("urls.login")
  lazy val loginContinueUrl: String = config.get[String]("urls.loginContinue")
  lazy val unauthorisedUrl: String = config.get[String]("urls.unauthorised")
  lazy val alreadyClaimedUrl: String = config.get[String]("urls.alreadyClaimed")
  lazy val trustNotClaimedUrl: String = config.get[String]("urls.trustNotClaimed")
  lazy val agentNotAuthorisedUrl: String = config.get[String]("urls.agentNotAuthorised")
  lazy val createAgentServicesAccountUrl: String = config.get[String]("urls.createAgentServicesAccount")

  def claimATrustUrl(utr: String) =
    s"${config.get[String]("urls.startClaimATrust")}/$utr"

  def verifyIdentityForATrustUrl(utr: String) =
    s"${config.get[String]("urls.startVerifyIdentity")}/$utr"

  lazy val relationshipName: String =
    config.get[String]("microservice.services.self.relationship-establishment.name")
  lazy val relationshipIdentifier: String =
    config.get[String]("microservice.services.self.relationship-establishment.identifier")

  lazy val enrolmentStoreProxyUrl: String = config.get[Service]("microservice.services.enrolment-store-proxy").baseUrl

}
