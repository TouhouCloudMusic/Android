package net.hearnsoft.tcm.compose.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlayerFavoriteBridge {
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun update(isFavorite: Boolean) {
        _isFavorite.value = isFavorite
    }

    fun toggle(): Boolean {
        _isFavorite.value = !_isFavorite.value
        return _isFavorite.value
    }
}