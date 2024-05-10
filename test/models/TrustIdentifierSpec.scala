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

package models

import base.SpecBase

class TrustIdentifierSpec extends SpecBase {

  "TrustIdentifier" should {
    "create a UTR when passed a UTR" in {
      TrustIdentifier("1234567890") mustEqual UTR("1234567890")
      TrustIdentifier("1111111111") mustEqual UTR("1111111111")
    }

    "create a URN when passed a non-UTR" in {
      TrustIdentifier("XATRUS12345678") mustEqual URN("XATRUS12345678")
      TrustIdentifier("111") mustEqual URN("111")
    }
  }

}
