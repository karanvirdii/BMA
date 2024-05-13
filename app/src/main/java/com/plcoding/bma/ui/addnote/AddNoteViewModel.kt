package com.plcoding.bma.ui.addnote

import android.util.Patterns
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.load
import com.plcoding.bma.R
import com.plcoding.bma.data.NoteDao
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.util.folders
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.absoluteValue

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val dao: NoteDao
) : ViewModel() {

    private val _titleError = MutableSharedFlow<String>()
    val titleError: SharedFlow<String> = _titleError

    private val _urlError = MutableSharedFlow<String>()
    val urlError: SharedFlow<String> = _urlError

    private val _addNote = MutableSharedFlow<Unit>()
    val addNote: SharedFlow<Unit> = _addNote

    private val _selectedFolder = MutableStateFlow(folders.first())
    val selectedFolder: StateFlow<Folder> = _selectedFolder

    private val _addNoteLoading = MutableStateFlow(false)
    val addNoteLoading = _addNoteLoading.asStateFlow()

    fun selectFolder(folder: Folder) {
        _selectedFolder.value = folder
    }

    fun validateNote(
        note: Note,
        titleEmptyErrorText: String,
        urlEmptyErrorText: String,
        invalidUrlErrorText: String
    ) {
        viewModelScope.launch {
            var hasError = false
            if (note.title.isEmpty()) {
               _titleError.emit(titleEmptyErrorText)
                hasError = true
            }
            if (note.url.isEmpty()) {
                _urlError.emit(urlEmptyErrorText)
                hasError = true
            } else if (!isValidUrl(note.url)) {
                _urlError.emit(invalidUrlErrorText)
                hasError = true
            }
            if (!hasError) {
                _addNoteLoading.value = true
                val noteToInsert = if (!note.url.contains("://")) {
                    note.copy(url = "https://${note.url}")
                } else note
                val preview = RichPreview(object : ResponseListener {
                    override fun onData(metaData: MetaData?) {
                        metaData?.imageurl?.let { url ->
                            viewModelScope.launch {
                                if (url.isNotEmpty()) {
                                    //dao.insertNote(noteToInsert.copy(previewImageUrl = url))
                                     dao.insertNote(noteToInsert.copy(previewImageUrl = url, title = metaData.title, description = metaData.description))
                                    _addNote.emit(Unit)
                                } else {
                                    dao.insertNote(noteToInsert)
                                    _addNote.emit(Unit)
                                }
                                _addNoteLoading.value = false
                            }

                        }
                    }

                    override fun onError(e: Exception?) {
                        viewModelScope.launch {
                            dao.insertNote(noteToInsert)
                            _addNote.emit(Unit)
                            _addNoteLoading.value = false
                        }
                    }
                })
                preview.getPreview(noteToInsert.url)
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        val isValidPattern = Patterns.WEB_URL.matcher(url).matches()
        val hasValidProtocol = url.startsWith("http://") || url.startsWith("https://") ||
                !url.contains("://")
        return isValidPattern && hasValidProtocol
    }

}