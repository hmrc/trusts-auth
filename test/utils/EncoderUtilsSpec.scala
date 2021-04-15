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

package utils

import org.scalacheck.Gen
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.EncoderUtils._
import wolfendale.scalacheck.regexp.RegexpGen

class EncoderUtilsSpec extends PlaySpec with ScalaCheckPropertyChecks {

  "EncoderUtils" must {

    "be able to encode a string and decode it to the original string" in {

      val gen: Gen[String] = RegexpGen.from("^[A-Z0-9]{6}$")

      forAll(gen) { string =>
        val encodedString = encode(string)
        encodedString mustNot equal(string)
        val decodedString = decode(encodedString)
        decodedString mustEqual string
      }
    }
  }
}
