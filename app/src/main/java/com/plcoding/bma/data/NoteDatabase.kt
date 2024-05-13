package com.plcoding.bma.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plcoding.bma.R
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.data.models.FolderList
import com.plcoding.bma.data.models.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    //entities = [Note::class, Folder::class],
    entities = [Note::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase: RoomDatabase() {

    abstract val dao: NoteDao

}