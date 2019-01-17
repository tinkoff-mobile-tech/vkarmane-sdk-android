/*
 * Copyright © 2019 Tinkoff Bank
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

package ru.tinkoff.vkarmane.sdk.constants

enum class RusDriverCategory(val code: Int) {
    A(0),
    B(1),
    C(2),
    D(3),
    BE(4),
    CE(5),
    DE(6),
    TM(7),
    TB(8),
    A1(9),
    B1(10),
    C1(11),
    D1(12),
    C1E(13),
    D1E(14),
    AM(16),
    UNKNOWN(-1);

    companion object {
        @JvmStatic
        fun fromInt(value: Int): RusDriverCategory {
            return values().firstOrNull { value == it.code } ?: UNKNOWN
        }
    }
}