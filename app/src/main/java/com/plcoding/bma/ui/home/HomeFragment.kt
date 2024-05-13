package com.plcoding.bma.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.plcoding.bma.adapter.HorizontalFolderAdapter
import com.plcoding.bma.R
import com.plcoding.bma.adapter.FolderAdapter
import com.plcoding.bma.databinding.FragmentHomeBinding
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.ui.MainActivity
import com.plcoding.bma.util.Constants.FOLDER_NAME_FAVORITES
import com.plcoding.bma.util.Constants.FOLDER_NAME_RECENTLY_ADDED
import com.plcoding.bma.util.folders
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BindingFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentHomeBinding::inflate

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var hrvfolderAdapter: HorizontalFolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        folderAdapter = FolderAdapter { folder ->
            when (folder.name) {
                FOLDER_NAME_FAVORITES -> {
                    (requireActivity() as? MainActivity)?.switchToBottomNavTab(R.id.favoritesFragment)
                }
                FOLDER_NAME_RECENTLY_ADDED -> {
                    (requireActivity() as? MainActivity)?.switchToBottomNavTab(R.id.historyFragment)
                }

                else -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToNoteDetailFragment(folder)
                    )
                }
            }
        }

        hrvfolderAdapter = HorizontalFolderAdapter { folder ->
            when (folder.name) {
                FOLDER_NAME_FAVORITES -> {
                    (requireActivity() as? MainActivity)?.switchToBottomNavTab(R.id.favoritesFragment)
                }
                FOLDER_NAME_RECENTLY_ADDED -> {
                    (requireActivity() as? MainActivity)?.switchToBottomNavTab(R.id.historyFragment)
                }

                else -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToNoteDetailFragment(folder)
                    )
                }
            }
        }
        binding.gvFolders.adapter = folderAdapter
        folderAdapter.folders = folders


        binding.hrvHorizontalList.apply {
            adapter  = hrvfolderAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        hrvfolderAdapter.submitList(folders)
        //binding.tvTitle.centre

        binding.fabAddFolder.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddFolderFragment()
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miSettings -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                )
            }
            R.id.miInfo -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToInfoFragment()
                )
            }
            R.id.fabAddFolder -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddFolderFragment()
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}