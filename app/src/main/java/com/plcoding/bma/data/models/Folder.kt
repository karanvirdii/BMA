package com.plcoding.bma.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

//@Entity
//Entity refers to the relationship of more than one entity in the dao
@Parcelize
data class Folder(
    val name: String,
    val imageResId: Int,
    @PrimaryKey
    val folderId: Int? = null
): Parcelable
