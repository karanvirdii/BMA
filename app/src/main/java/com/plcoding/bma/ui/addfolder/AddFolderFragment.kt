package com.plcoding.bma.ui.addfolder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.R
import com.plcoding.bma.databinding.FragmentAddFolderBinding
import com.plcoding.bma.ui.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.util.folders

@AndroidEntryPoint
class AddFolderFragment: BindingFragment<FragmentAddFolderBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAddFolderBinding::inflate

    //val folder: List<Folder> = listOf<Folder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Snackbar.make(view, "Hello, World!", Snackbar.LENGTH_SHORT).show()

        binding.btnAddFolder.setOnClickListener {
            Snackbar.make(view, "Hello, World!", Snackbar.LENGTH_SHORT).show()
        }

        folders.add(Folder(name = "Test", imageResId = R.drawable.ic_bookmark))
        folders.remove(Folder(name = "BMA", imageResId = R.drawable.ic_bookmark))

    }


}