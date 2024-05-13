package com.plcoding.bma.ui.notedetail

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.MainViewModel
import com.plcoding.bma.R
import com.plcoding.bma.adapter.NoteAdapter
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.databinding.*
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.ui.addnote.AddNoteFragmentDirections
import com.plcoding.bma.util.Constants.SEARCH_DELAY
import com.plcoding.bma.util.show
import com.plcoding.bma.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class NoteDetailFragment : BindingFragment<FragmentNoteDetailBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentNoteDetailBinding::inflate

    private val viewModel: NoteDetailViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private val args: NoteDetailFragmentArgs by navArgs()

    private lateinit var noteAdapter: NoteAdapter

    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToStateUpdates()
        viewModel.getNotesForFolder("", args.folder.name)

        noteAdapter = NoteAdapter(
            onFavoriteClick = { note, isFavorite ->
                viewModel.toggleIsFavorite(note)
                snackbar(
                    if (isFavorite) R.string.note_added_to_favorites
                    else R.string.note_removed_from_favorites,
                    view = binding.coordinatorLayout
                )
            },
            onEditClick = { note ->
                findNavController().navigate(
                    AddNoteFragmentDirections.globalActionToAddNoteFragment(
                        args.folder,
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
                viewModel.deleteNote(note)
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

        binding.tvFolderTitle.text = args.folder.name
        binding.ivNoteFolder.load(args.folder.imageResId) {
            crossfade(true)
        }

        //trying to open and close keyboard when textfield is empty or closed search on lifecycleScope.launch{}
        binding.etSearch.addTextChangedListener {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                if(it == null) {
                    searchJob?.cancel()
                } else {
                    delay(SEARCH_DELAY)
                    viewModel.getNotesForFolder(it.toString(), args.folder.name)
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentToAddNoteFragment(
                    args.folder,
                    null
                )
            )
        }
    }

    private fun deleteNote(note: Note) {
        viewModel.deleteNote(note)
        Snackbar.make(binding.coordinatorLayout, getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
            .apply {
                setAction(R.string.undo) {
                    viewModel.insertNote(note)
                }
                setActionTextColor(Color.WHITE)
            }.show()
    }

    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).also {
            it.data = Uri.parse(url)
        }

        if(isIntentAvailable(intent)) {
            startActivity(intent)
        } else {
            snackbar(R.string.error_no_browser_apps, view = binding.coordinatorLayout)
        }
    }

    private fun isIntentAvailable(intent: Intent): Boolean {
        val list = requireContext().packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }

    private fun subscribeToStateUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.notes.collect { notes ->
                binding.tvNoNotes.isVisible = notes.isEmpty() && !viewModel.isSearching.value
                noteAdapter.submitList(notes)
            }
        }
        lifecycleScope.launchWhenStarted {
            mainViewModel.addNoteEvent.collect {
                snackbar(R.string.note_added, view = binding.coordinatorLayout)
            }
        }
    }

    private fun setupRecyclerView() = binding.rvNotes.apply {
        adapter = noteAdapter
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        ItemTouchHelper(noteSwipeDeleteHelper).attachToRecyclerView(binding.rvNotes)
        itemAnimator = (itemAnimator as SimpleItemAnimator).apply {
            supportsChangeAnimations = false
        }
    }

    private val noteSwipeDeleteHelper =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = viewModel.notes.value[viewHolder.adapterPosition]
                mainViewModel.loadedAd.value?.let { ad ->
                    ad.show(
                        requireActivity(),
                        onNewAdLoaded = { mainViewModel.setLoadedAd(it) },
                        onAdDismissed = { deleteNote(note) },
                        onFullScreenFailed = { deleteNote(note) },
                        shouldShow = mainViewModel.shouldShowAds.value
                    )
                } ?: kotlin.run { deleteNote(note) }
            }
        }
}