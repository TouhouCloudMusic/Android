package net.hearnsoft.tcm.compose.domain.model.song

data class SongSortingRule(
    val strategy: SongSortingStrategy,
    val reverse: Boolean = false
)