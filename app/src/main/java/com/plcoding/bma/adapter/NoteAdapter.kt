package com.plcoding.bma.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.plcoding.bma.R
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.databinding.ItemNoteBinding
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.Exception

class NoteAdapter(
    private val onFavoriteClick: (Note, Boolean) -> Unit,
    private val onEditClick: (Note) -> Unit,
    private val onShareClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(Companion) {

    inner class NoteViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            ItemNoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = currentList[position]
        val context = holder.itemView.context
        holder.binding.apply {
            tvNoteTitle.text = note.title 
            tvNoteDescription.text = note.description ?: ""
            tvNoteFolder.text = note.folderName

            val resId = context.resources.getIdentifier(
                note.folderResIconName,
                "drawable",
                context.packageName
            )
            ivNoteFolder.load(resId) {
                crossfade(true)
            }

            if (note.isFavorite) {
                ivFavorite.load(R.drawable.ic_baseline_favorite_red_36)
            } else {
                ivFavorite.load(R.drawable.ic_baseline_favorite_border_36)
            }

            note.previewImageUrl?.let { url ->
                ivLinkPreview.load(url) {
                    crossfade(true)
                    error(resId)
                }
            } ?: ivLinkPreview.load(resId) {
                crossfade(true)
            }

            ivFavorite.setOnClickListener {
                onFavoriteClick(note, !note.isFavorite)
            }
            ivEdit.setOnClickListener {
                onEditClick(note)
            }
            ivShare.setOnClickListener {
                onShareClick(note)
            }
            ivDelete.setOnClickListener {
                onDeleteClick(note)
            }
            root.setOnClickListener {
                onNoteClick(note)
            }


        }


    }
}