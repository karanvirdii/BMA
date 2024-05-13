package com.plcoding.bma

import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.room.Dao
import com.plcoding.bma.data.NoteDao
import com.plcoding.bma.data.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


abstract class CustomNoteDetailViewModel(
    private val dao: NoteDao
): ViewModel() {

    protected abstract val currentListFlow: StateFlow<List<Note>>

    protected val listOfNotes: Flow<List<Note>> = dao.getAllNotes()

    protected val mutableNotes: MutableStateFlow<List<Note>> = MutableStateFlow(listOf())
    val notes: Flow<List<Note>> = dao.getAllNotes()

    protected val _isSearchingList = MutableStateFlow(false)
    val isSearchingNotes = _isSearchingList.asStateFlow()

}