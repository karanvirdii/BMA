package com.plcoding.bma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.plcoding.bma.data.models.InfoItem
import com.plcoding.bma.databinding.ItemFolderSpinnerBinding

class InfoAdapter(
    private val onItemClick: (InfoItem) -> Unit,
) : ListAdapter<InfoItem, InfoAdapter.NoteViewHolder>(Companion) {

    inner class NoteViewHolder(val binding: ItemFolderSpinnerBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<InfoItem>() {
        override fun areItemsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            ItemFolderSpinnerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val item = currentList[position]
        holder.binding.apply {
            tvNoteFolder.text = item.name
            ivNoteFolder.load(item.resId) {
                crossfade(true)
            }

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}