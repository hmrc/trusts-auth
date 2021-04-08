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

package config

import com.typesafe.config.ConfigList

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

@Singleton
class AppConfig @Inject()(val config: Configuration) {

  val TAXABLE_ENROLMENT = "HMRC-TERS-ORG"
  val TAXABLE_ENROLMENT_ID = "SAUTR"
  val NON_TAXABLE_ENROLMENT = "HMRC-TERSNT-ORG"
  val NON_TAXABLE_ENROLMENT_ID = "URN"

  val authBaseUrl: String = config.get[Service]("microservice.services.auth").baseUrl

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

  lazy val unauthorisedUrl: String = config.get[String]("urls.unauthorised")
  lazy val alreadyClaimedUrl: String = config.get[String]("urls.alreadyClaimed")
  lazy val trustNotClaimedUrl: String = config.get[String]("urls.trustNotClaimed")
  lazy val agentNotAuthorisedUrl: String = config.get[String]("urls.agentNotAuthorised")
  lazy val createAgentServicesAccountUrl: String = config.get[String]("urls.createAgentServicesAccount")
  lazy val maintainThisTrust: String = config.get[String]("urls.maintainThisTrust")

  def claimATrustUrl(utr: String) =
    s"${config.get[String]("urls.startClaimATrust")}/$utr"

  lazy val relationshipName: String =
    config.get[String]("microservice.services.self.relationship-establishment.name")
  lazy val taxableRelationshipIdentifier: String =
    config.get[String]("microservice.services.self.relationship-establishment.taxable.identifier")
  lazy val nonTaxableRelationshipIdentifier: String =
    config.get[String]("microservice.services.self.relationship-establishment.nonTaxable.identifier")

  lazy val enrolmentStoreProxyUrl: String = config.get[Service]("microservice.services.enrolment-store-proxy").baseUrl

  lazy val accessCodes: List[String] = config.get[ConfigList]("accessCodes")
    .unwrapped()
    .toList
    .map(accessCode => Base64.getDecoder.decode(accessCode.toString))
    .map(new String(_, StandardCharsets.UTF_8))

}
