package com.plcoding.bma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.databinding.ItemFolderBinding

class HorizontalFolderAdapter(
    private val onFolderClick: (Folder) -> Unit
) : ListAdapter<Folder, HorizontalFolderAdapter.NoteViewHolder>(Companion) {

    inner class NoteViewHolder(val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            ItemFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val folder = currentList[position]
        holder.binding.apply {
            ivFolderImage?.setImageResource(folder.imageResId)
            tvFolderTitle?.text = folder.name

            root.setOnClickListener {
                onFolderClick(folder)
            }
        }
    }
}