package com.plcoding.bma.ui.history

import androidx.lifecycle.viewModelScope
import com.plcoding.bma.CustomNoteDetailViewModel
import com.plcoding.bma.data.NoteDao
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.ui.BaseNoteDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModelCustom @Inject constructor(
    private val dao: NoteDao
) : CustomNoteDetailViewModel(dao) {


    override var currentListFlow: StateFlow<List<Note>> = MutableStateFlow(listOf())
    private var job: Job? = null

    init {
        getHistorySearched("")
    }

    fun getHistorySearched(query: String) {
        _isSearchingList.value = query.isNotEmpty()
        currentListFlow = dao.searchHistory(query).stateIn(viewModelScope, SharingStarted.Lazily, listOf())
        job?.cancel()
        job = viewModelScope.launch {
            currentListFlow.collect { notes ->
                mutableNotes.value = notes
            }
        }
    }





}