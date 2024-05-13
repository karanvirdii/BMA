package com.plcoding.bma.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class FolderList(
    val name: String,
    val imageResId: Int,
    @PrimaryKey
    val folderId: Int? = null
): Parcelable
