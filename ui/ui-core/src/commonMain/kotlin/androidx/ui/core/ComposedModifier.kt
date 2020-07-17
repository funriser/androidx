/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.ui.core

import androidx.compose.Composable
import androidx.compose.ComposeCompilerApi
import androidx.compose.Composer

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have
 * instance-specific state for each modified element, allowing the same [Modifier] instance to be
 * safely reused for multiple elements while maintaining element-specific state.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly
 * applying a [Modifier] to an element tree node.
 */
fun Modifier.composed(
    factory: @Composable Modifier.() -> Modifier
): Modifier = this.then(ComposedModifier(factory))

private data class ComposedModifier(
    val factory: @Composable Modifier.() -> Modifier
) : Modifier.Element

/**
 * Materialize any instance-specific [composed modifiers][composed] for applying to a raw tree node.
 * Call right before setting the returned modifier on an emitted node.
 * You almost certainly do not need to call this function directly.
 */
@OptIn(ComposeCompilerApi::class)
fun Composer<*>.materialize(modifier: Modifier): Modifier {
    if (modifier.all { it !is ComposedModifier }) return modifier

    // This is a fake composable function that invokes the compose runtime directly so that it
    // can call the element factory functions from the non-@Composable lambda of Modifier.foldIn.
    // It would be more efficient to redefine the Modifier type hierarchy such that the fold
    // operations could be inlined or otherwise made cheaper, which could make this unnecessary.

    // Random number for fake group key. Chosen by fair die roll.
    startReplaceableGroup(0x48ae8da7)

    val result = modifier.foldIn<Modifier>(Modifier) { acc, element ->
        acc.then(
            if (element is ComposedModifier) {
                @kotlin.Suppress("UNCHECKED_CAST")
                val factory = element.factory as Modifier.(Composer<*>, Int, Int) -> Modifier
                val composedMod = factory(Modifier, this, 0, 0)
                materialize(composedMod)
            } else element
        )
    }

    endReplaceableGroup()
    return result
}
