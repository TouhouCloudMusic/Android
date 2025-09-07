package net.hearnsoft.tcm.compose.domain.model.album

data class AlbumSortingRule(
    val strategy: AlbumSortingStrategy,
    val reverse: Boolean = false
)