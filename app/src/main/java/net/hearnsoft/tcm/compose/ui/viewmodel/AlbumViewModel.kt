package net.hearnsoft.tcm.compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.data.database.dao.AlbumDao
import net.hearnsoft.tcm.compose.data.database.dao.SongDao
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumDao: AlbumDao,
    private val songDao: SongDao
) : ViewModel() {

    private val _currentAlbum = MutableStateFlow<AlbumEntity?>(null)
    val currentAlbum: StateFlow<AlbumEntity?> = _currentAlbum.asStateFlow()

    private val _albumSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val albumSongs: StateFlow<List<SongEntity>> = _albumSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentAlbum.value = albumDao.getAlbumById(albumId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAlbumSongs(albumId: Long) {
        viewModelScope.launch {
            songDao.getSongsByAlbumOrdered(albumId).collect { songs ->
                _albumSongs.value = songs
            }
        }
    }
}