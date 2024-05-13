package com.plcoding.bma.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import coil.load
import com.plcoding.bma.R
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.databinding.ItemFolderSpinnerBinding

class FolderSpinnerAdapter(
    context: Context,
    private val folders: List<Folder>
) : ArrayAdapter<String>(context, R.layout.item_folder_spinner) {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int): String {
        return folders[position].name
    }

    override fun getItemId(position: Int): Long {
        return folders[position].hashCode().toLong()
    }

    override fun getCount(): Int {
        return folders.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val folder = folders[position]
        val view = convertView ?: inflater.inflate(R.layout.item_folder_spinner, parent, false)

        val ivNoteFolder = view.findViewById<ImageView>(R.id.ivNoteFolder)
        val tvNoteFolder = view.findViewById<TextView>(R.id.tvNoteFolder)
        ivNoteFolder.load(folder.imageResId) {
            crossfade(true)
        }
        tvNoteFolder?.text = folder.name
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = folders
                    count = folders.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}