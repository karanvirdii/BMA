package com.plcoding.bma.ui.history

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
class HistoryViewModel @Inject constructor(
    private val dao: NoteDao
) : BaseNoteDetailViewModel(dao) {

    override var currentStateFlow: StateFlow<List<Note>> = MutableStateFlow(listOf())
    private var currentJob: Job? = null

    init {
        getHistory("")
    }

    fun getHistory(query: String) {
        _isSearching.value = query.isNotEmpty()
        currentStateFlow = dao.searchHistory(query).stateIn(viewModelScope, SharingStarted.Lazily, listOf())
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            currentStateFlow.collect { notes ->
                mutableNotes.value = notes
            }
        }
    }


    fun getSearchedHistory(query: String) {
        _isSearching.value = query.isNotEmpty()

    }

}