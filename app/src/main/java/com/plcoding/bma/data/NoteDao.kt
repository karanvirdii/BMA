package com.plcoding.bma.data

import androidx.room.*
import com.plcoding.bma.data.models.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note WHERE folderName = :folderName ORDER BY noteId desc, timestamp")
    fun getNotesFromFolder(folderName: String): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE isFavorite order by noteId")
    fun getFavoriteNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note ORDER BY noteId")
    fun getRecentlyAddedNotes(): Flow<List<Note>>

    @Query("select * from note ORDER BY noteId desc, timestamp")
    fun getAllNotes(): Flow<List<Note>>

    @Query("""
        SELECT *
        FROM note
        WHERE folderName = :folderName AND 
            (lower(title) || lower(description)) LIKE ('%' || lower(:query) || '%')
    """)
    fun searchNoteFolder(query: String, folderName: String): Flow<List<Note>>

    @Query("""
        SELECT *
        FROM note
        WHERE isFavorite AND 
            (lower(title) || lower(description)) LIKE ('%' || lower(:query) || '%')
    """)
    fun searchFavorites(query: String): Flow<List<Note>>

    @Query("""
        SELECT *
        FROM note
        WHERE isInHistory AND 
            (lower(title) || lower(description)) LIKE ('%' || lower(:query) || '%')
    """)
    fun searchHistory(query: String): Flow<List<Note>>

}