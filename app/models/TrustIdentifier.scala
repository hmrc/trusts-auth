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

sealed trait TrustIdentifier{
  val value: String
}
final case class UTR(value: String) extends TrustIdentifier
final case class URN(value: String) extends TrustIdentifier

object TrustIdentifier {
  private val utrRegex = "^[0-9]{10}$".r.pattern

  def apply(identifier: String): TrustIdentifier =
    if (utrRegex.matcher(identifier).matches) UTR(identifier) else URN(identifier)
}
