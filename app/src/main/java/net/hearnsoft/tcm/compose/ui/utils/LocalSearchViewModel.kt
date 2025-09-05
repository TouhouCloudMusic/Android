package net.hearnsoft.tcm.compose.ui.utils

import androidx.compose.runtime.compositionLocalOf
import net.hearnsoft.tcm.compose.ui.viewmodel.SearchViewModel

val LocalSearchViewModel = compositionLocalOf<SearchViewModel?> { null }