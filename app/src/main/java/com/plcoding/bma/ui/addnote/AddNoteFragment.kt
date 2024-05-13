package com.plcoding.bma.ui.addnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.MainViewModel
import com.plcoding.bma.R
import com.plcoding.bma.adapter.FolderSpinnerAdapter
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.databinding.FragmentAddNoteBinding
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.ui.MainActivity
import com.plcoding.bma.util.Constants
import com.plcoding.bma.util.folders
import com.plcoding.bma.util.show
import com.plcoding.bma.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import java.lang.Exception

@AndroidEntryPoint
class AddNoteFragment : BindingFragment<FragmentAddNoteBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentAddNoteBinding::inflate

    private val viewModel: AddNoteViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private val args: AddNoteFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToStateUpdates()

        initFolderSpinner()

        binding.cbAutoFillNote.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked) {
                args.note?.let { note ->
                    val link_preview = RichPreview(object: ResponseListener {
                        override fun onData(metaData: MetaData?) {
                            binding.etNoteTitle.setText(metaData?.title)
                            binding.etNoteDescription.setText(metaData?.description)
                            binding.etNoteUrl.setText(metaData?.url)
                        }

                        override fun onError(e: Exception?) {
                            Snackbar.make(view, "Error", Snackbar.LENGTH_SHORT).show()
                        }

                    })

                    link_preview.getPreview(note.url)
                }
            } else {
                args.note?.let { note ->
                    binding.etNoteTitle.setText(note.title)
                    binding.etNoteDescription.setText(note.description)
                    binding.etNoteUrl.setText(note.url)
                }
            }
        }

        //Snackbar.make(view, viewModel.getTitlePreview("https://www.youtube.com"), Snackbar.LENGTH_LONG).show()

        args.note?.let { note ->
            binding.etNoteTitle.setText(note.title)
            binding.etNoteDescription.setText(note.description)
            binding.etNoteUrl.setText(note.url)
        }

        binding.btnAddNote.text = if(args.note == null || args.isFromIntent) {
            getString(R.string.add)
        } else {
            getString(R.string.update)
        }



        binding.btnAddNote.setOnClickListener {
            val resName = resources.getResourceEntryName(viewModel.selectedFolder.value.imageResId)
            viewModel.validateNote(
                Note(
                    binding.etNoteTitle.text.toString().trim(),
                    binding.etNoteUrl.text.toString().trim(),
                    folderName = viewModel.selectedFolder.value.name,
                    folderResIconName = resName,
                    description = binding.etNoteDescription.text.toString().trim(),
                    isFavorite = args.note?.isFavorite ?: false,
                    isInHistory = args.note?.isInHistory ?: true,
                    noteId = args.note?.noteId
                ),
                getString(R.string.error_empty_title),
                getString(R.string.error_empty_url),
                getString(R.string.error_invalid_url)
            )
        }
    }

    private fun subscribeToStateUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.addNote.collect {
                mainViewModel.loadedAd.value?.let { ad ->
                    ad.show(
                        requireActivity(),
                        onAdDismissed = {
                            showAddedSnackbarAndPopBackStack()
                        },
                        onFullScreenFailed = {
                            showAddedSnackbarAndPopBackStack()
                        },
                        onNewAdLoaded = { mainViewModel.setLoadedAd(it) },
                        shouldShow = mainViewModel.shouldShowAds.value
                    )
                } ?: kotlin.run {
                    showAddedSnackbarAndPopBackStack()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.addNoteLoading.collect { isLoading ->
                binding.addNoteProgressBar.isVisible = isLoading
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.titleError.collect { error ->
                binding.etNoteTitle.error = error
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.urlError.collect { error ->
                binding.etNoteUrl.error = error
            }
        }
    }

    private fun showAddedSnackbarAndPopBackStack() {
        lifecycleScope.launch {
            delay(100L)
            findNavController().popBackStack()
            mainViewModel.sendAddNoteEvent()
        }
    }

    private fun initFolderSpinner() {
        val foldersWithNotes = folders.filter {
            it.name != Constants.FOLDER_NAME_RECENTLY_ADDED &&
                    it.name != Constants.FOLDER_NAME_FAVORITES
        }
        val adapter = FolderSpinnerAdapter(requireContext(), foldersWithNotes)
        binding.tvFolder.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.selectFolder(folders[position])
            }
        binding.tvFolder.setAdapter(adapter)
        binding.tvFolder.setText(args.folder.name, false)
        viewModel.selectFolder(args.folder)
    }
}