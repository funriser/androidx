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

package androidx.ui.core.selection

import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.ClipboardManagerAmbient
import androidx.ui.core.HapticFeedBackAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.TextToolbarAmbient
import androidx.ui.core.gesture.dragGestureFilter
import androidx.ui.core.gesture.longPressDragGestureFilter
import androidx.ui.core.gesture.tapGestureFilter
import androidx.ui.core.onPositioned
import androidx.compose.ui.text.InternalTextApi

/**
 * Default SelectionContainer to be used in order to make composables selectable by default.
 */
@Composable
internal fun SelectionContainer(children: @Composable () -> Unit) {
    val selection = state<Selection?> { null }
    SelectionContainer(
        selection = selection.value,
        onSelectionChange = { selection.value = it },
        children = children
    )
}

/**
 * Selection Composable.
 *
 * The selection composable wraps composables and let them to be selectable. It paints the selection
 * area with start and end handles.
 */
@OptIn(InternalTextApi::class)
@Composable
fun SelectionContainer(
    /** Current Selection status.*/
    selection: Selection?,
    /** A function containing customized behaviour when selection changes. */
    onSelectionChange: (Selection?) -> Unit,
    children: @Composable () -> Unit
) {
    val registrarImpl = remember { SelectionRegistrarImpl() }
    val manager = remember { SelectionManager(registrarImpl) }

    manager.hapticFeedBack = HapticFeedBackAmbient.current
    manager.clipboardManager = ClipboardManagerAmbient.current
    manager.textToolbar = TextToolbarAmbient.current
    manager.onSelectionChange = onSelectionChange
    manager.selection = selection

    val gestureModifiers =
        Modifier
            .tapGestureFilter { manager.onRelease() }
            .longPressDragGestureFilter(manager.longPressDragObserver)

    val modifier = remember {
        // Get the layout coordinates of the selection container. This is for hit test of
        // cross-composable selection.
        gestureModifiers.onPositioned { manager.containerLayoutCoordinates = it }
    }

    Providers(SelectionRegistrarAmbient provides registrarImpl) {
        // Get the layout coordinates of the selection container. This is for hit test of
        // cross-composable selection.
        SelectionLayout(modifier) {
            children()
            manager.selection?.let {
                for (isStartHandle in listOf(true, false)) {
                    SelectionHandleLayout(
                        startHandlePosition = manager.startHandlePosition,
                        endHandlePosition = manager.endHandlePosition,
                        isStartHandle = isStartHandle,
                        directions = Pair(it.start.direction, it.end.direction),
                        handlesCrossed = it.handlesCrossed
                    ) {
                        SelectionHandle(
                            modifier =
                            Modifier.dragGestureFilter(manager.handleDragObserver(isStartHandle)),
                            isStartHandle = isStartHandle,
                            directions = Pair(it.start.direction, it.end.direction),
                            handlesCrossed = it.handlesCrossed
                        )
                    }
                }
                SelectionFloatingToolBar(manager = manager)
            }
        }
    }
}

@Composable
private fun SelectionFloatingToolBar(manager: SelectionManager) {
    manager.showSelectionToolbar()
}
