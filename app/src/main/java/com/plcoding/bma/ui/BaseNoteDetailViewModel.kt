package com.plcoding.bma.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bma.data.NoteDao
import com.plcoding.bma.data.models.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.State


abstract class BaseNoteDetailViewModel(
    private val dao: NoteDao
) : ViewModel() {

    protected abstract var currentStateFlow: StateFlow<List<Note>>


    protected val mutableNotes: MutableStateFlow<List<Note>> = MutableStateFlow(listOf())
    val notes: StateFlow<List<Note>> = mutableNotes

    //val bookmarks: Flow<List<Note>> = dao.getNotesFromFolder()
    //val allNotes: Flow<List<Note>> = dao.getAllNotes()

    protected val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun insertNote(note: Note) {
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }

    fun toggleIsFavorite(note: Note) {
        viewModelScope.launch {
            dao.insertNote(
                note.copy(
                    isFavorite = !note.isFavorite
                )
            )
        }
    }

}