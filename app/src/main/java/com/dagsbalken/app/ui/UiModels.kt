package com.dagsbalken.app.ui

import androidx.compose.runtime.Immutable
import com.dagsbalken.core.data.CustomBlock

/**
 * Immutable wrapper for CustomBlock to make it stable in Compose.
 * This allows LazyColumn/Lists to skip recomposition of items that haven't changed.
 */
@Immutable
data class UiCustomBlock(val block: CustomBlock)
