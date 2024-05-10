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

package config

import base.SpecBase

class ServiceSpec extends SpecBase {

  "Test that the baseUrl generated correctly when all strings are empty" should {
    ".baseUrl" in {
      val service = Service("", "", "")
      service.baseUrl mustBe "://:"
    }
    ".toString" in {
      val service = Service("", "", "")
      service.toString mustBe "://:"
    }
    ".convertToString" in {
      val service = Service("", "", "")
      Service.convertToString(service) mustBe "://:"
    }
  }

  "Test that the baseUrl generated correctly when all strings are populated" should {
    ".baseUrl" in {
      val service = Service("host", "port", "protocol")
      service.baseUrl mustBe "protocol://host:port"
    }
    ".toString" in {
      val service = Service("host", "port", "protocol")
      service.toString mustBe "protocol://host:port"
    }
    ".convertToString" in {
      val service = Service("host", "port", "protocol")
      Service.convertToString(service) mustBe "protocol://host:port"
    }
  }
  
}
