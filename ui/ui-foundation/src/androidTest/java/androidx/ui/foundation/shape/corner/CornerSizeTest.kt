/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.foundation.shape.corner

import androidx.test.filters.SmallTest
import androidx.ui.unit.Density
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
import androidx.ui.unit.px
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@SmallTest
@RunWith(JUnit4::class)
class CornerSizeTest {

    private val density = Density(2.5f)
    private val size = PxSize(150.px, 300.px)

    @Test
    fun pxCorners() {
        val corner = CornerSize(24.0f)
        assertThat(corner.toPx(size, density)).isEqualTo(24.0f)
    }

    @Test
    fun dpCorners() {
        val corner = CornerSize(5.dp)
        assertThat(corner.toPx(size, density)).isEqualTo(12.5f)
    }

    @Test
    fun intPercentCorners() {
        val corner = CornerSize(15)
        assertThat(corner.toPx(size, density)).isEqualTo(22.5f)
    }

    @Test
    fun zeroCorners() {
        val corner = ZeroCornerSize
        assertThat(corner.toPx(size, density)).isEqualTo(0.0f)
    }

    @Test
    fun pxCornersAreEquals() {
        assertThat(CornerSize(24.0f)).isEqualTo(CornerSize(24.0f))
    }

    @Test
    fun dpCornersAreEquals() {
        assertThat(CornerSize(8.dp)).isEqualTo(CornerSize(8.dp))
    }
}
