package com.plcoding.bma.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.plcoding.bma.R
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.util.folders

class FolderAdapter(
    private val onFolderClick: (Folder) -> Unit
) : BaseAdapter() {

    var folders = listOf<Folder>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return folders.size
    }

    override fun getItem(position: Int): Any {
        return folders[position]
    }

    override fun getItemId(position: Int): Long {
        return folders[position].folderId!!.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val folder = folders[position]

        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val folderView = convertView ?: inflater?.inflate(R.layout.item_folder, parent, false)!!
        val ivFolderImage = folderView.findViewById<ImageView>(R.id.ivFolderImage)
        val tvFolderTitle = folderView.findViewById<TextView>(R.id.tvFolderTitle)

        ivFolderImage?.setImageResource(folder.imageResId)
        tvFolderTitle?.text = folder.name

        folderView.setOnClickListener {
            onFolderClick(folder)
        }
        return folderView
    }
}