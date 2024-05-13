package com.plcoding.bma.ui.notedetail

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
class NoteDetailViewModel @Inject constructor(
    private val dao: NoteDao
) : BaseNoteDetailViewModel(dao) {

    override var currentStateFlow: StateFlow<List<Note>> = MutableStateFlow(listOf())
    private var currentJob: Job? = null

    fun getNotesForFolder(query: String, folderName: String) {
        _isSearching.value = query.isNotEmpty()
        val noteList: Flow<List<Note>> = dao.searchNoteFolder(query, folderName)
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            noteList.collect { notes ->
                mutableNotes.value = notes
            }
        }
    }

    //dao.searchNoteFolder(query, folderName).stateIn(viewModelScope, SharingStarted.Lazily, listOf())

}