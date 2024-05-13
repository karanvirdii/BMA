package com.plcoding.bma.data.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Entity
@Parcelize
data class Note(
    val title: String,
    val url: String,
    val folderName: String,
    val folderResIconName: String,
    val description: String? = null,
    val isFavorite: Boolean = false,
    val isInHistory: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val previewImageUrl: String? = null,
    @PrimaryKey
    val noteId: Int? = null,
): Parcelable
