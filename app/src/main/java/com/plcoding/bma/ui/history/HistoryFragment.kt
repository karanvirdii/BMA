package com.plcoding.bma.ui.history

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.MainViewModel
import com.plcoding.bma.R
import com.plcoding.bma.adapter.NoteAdapter
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.databinding.FragmentHistoryBinding
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.ui.addnote.AddNoteFragmentDirections
import com.plcoding.bma.util.show
import com.plcoding.bma.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import java.util.Collections

@AndroidEntryPoint
class HistoryFragment : BindingFragment<FragmentHistoryBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentHistoryBinding::inflate

    private val viewModel: HistoryViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val cusViewModel: HistoryViewModelCustom by viewModels()

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteAdapter = NoteAdapter(
            onFavoriteClick = { note, isFavorite ->
                viewModel.toggleIsFavorite(note)
                snackbar(
                    if (isFavorite) R.string.note_added_to_favorites
                    else R.string.note_removed_from_favorites,
                    requireActivity().findViewById(R.id.bottomNavigationView)
                )
            },
            onEditClick = { note ->
                findNavController().navigate(
                    AddNoteFragmentDirections.globalActionToAddNoteFragment(
                        Folder(
                            note.folderName,
                            resources.getIdentifier(
                                note.folderResIconName,
                                "drawable",
                                requireContext().packageName
                            )
                        ),
                        note
                    )
                )
            },
            onShareClick = { note ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, note.url)
                    type = "text/*"
                }
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        resources.getText(R.string.share_with)
                    )
                )
            },
            onDeleteClick = { note ->
                mainViewModel.loadedAd.value?.let { ad ->
                    ad.show(
                        requireActivity(),
                        onNewAdLoaded = { mainViewModel.setLoadedAd(it) },
                        onAdDismissed = { deleteNote(note) },
                        onFullScreenFailed = { deleteNote(note) },
                        shouldShow = mainViewModel.shouldShowAds.value
                    )
                } ?: kotlin.run { deleteNote(note) }
            },
            onNoteClick = { note ->
                mainViewModel.loadedAd.value?.let { ad ->
                    ad.show(
                        requireActivity(),
                        onAdDismissed = { openUrlInBrowser(note.url) },
                        onFullScreenFailed = { openUrlInBrowser(note.url) },
                        onNewAdLoaded = { mainViewModel.setLoadedAd(it) },
                        shouldShow = mainViewModel.shouldShowAds.value
                    )
                } ?: kotlin.run {
                    openUrlInBrowser(note.url)
                }
            }
        )
        setupRecyclerView()
        //subscribeToStateUpdates()
        submitNotes()
        searchNoteHistory()

    }

    private fun searchNoteHistory() {
        binding.etSearch.addTextChangedListener {
            cusViewModel.getHistorySearched(it.toString())
        }
    }

    private fun deleteNote(note: Note) {
        viewModel.deleteNote(note)
        Snackbar.make(requireView(), getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
            .apply {
                setAction(R.string.undo) {
                    viewModel.insertNote(note)
                }
                setActionTextColor(Color.WHITE)
            }.apply {
                anchorView = requireActivity().findViewById(R.id.bottomNavigationView)
            }.show()
    }

    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).also {
            it.data = Uri.parse(url)
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            snackbar(
                R.string.error_no_browser_apps,
                requireActivity().findViewById(R.id.bottomNavigationView)
            )
        }
    }

    private fun subscribeToStateUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.notes.collect { notes ->
                binding.tvNoHistory.isVisible = notes.isEmpty() && !viewModel.isSearching.value
                noteAdapter.submitList(notes)
            }
        }
    }

    private fun submitNotes() {
        lifecycleScope.launchWhenCreated {
            cusViewModel.notes.collect { notes ->
                binding.tvNoHistory.isVisible = notes.isEmpty() && !cusViewModel.isSearchingNotes.value
                noteAdapter.submitList(notes)
            }
        }
    }

    private fun setupRecyclerView() = binding.rvNotes.apply {

        //Collections.sort(viewModel.notes)



        adapter = noteAdapter
        layoutManager = LinearLayoutManager(requireContext())
        itemAnimator = (itemAnimator as SimpleItemAnimator).apply {
            supportsChangeAnimations = false
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_menu, menu)
    }

}