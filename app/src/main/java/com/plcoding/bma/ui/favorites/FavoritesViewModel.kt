package com.plcoding.bma.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bma.data.NoteDao
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.ui.BaseNoteDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val dao: NoteDao
): BaseNoteDetailViewModel(dao) {

    override var currentStateFlow: StateFlow<List<Note>> = MutableStateFlow(listOf())
    private var currentJob: Job? = null
    var favoriteNotes: Flow<List<Note>> = dao.getFavoriteNotes()

    init {
        getFavorites("")
    }

    fun getFavorites(query: String) {
        _isSearching.value = query.isNotEmpty()
        currentStateFlow = dao.searchFavorites(query).stateIn(viewModelScope, SharingStarted.Lazily, listOf())
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            currentStateFlow.collect { notes ->
                mutableNotes.value = notes
            }
        }
    }

}