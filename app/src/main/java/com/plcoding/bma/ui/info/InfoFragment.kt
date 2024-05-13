package com.plcoding.bma.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.plcoding.bma.R
import com.plcoding.bma.adapter.InfoAdapter
import com.plcoding.bma.data.models.InfoItem
import com.plcoding.bma.data.models.InfoItem.InfoType.*
import com.plcoding.bma.databinding.*
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.util.snackbar

class InfoFragment : BindingFragment<FragmentInfoBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentInfoBinding::inflate

    private val infoItems = listOf(
        InfoItem("BMA on Instagram", R.drawable.ic_instagram, "https://www.instagram.com/bma_app_official/", LINK),
        InfoItem("BMA on Facebook", R.drawable.ic_facebook, "https://www.facebook.com/bma.app.9/", LINK),
        InfoItem("BMA on YouTube", R.drawable.ic_youtube, "https://www.youtube.com/channel/UCOP2AyLWxu94pkFN3KnuPfA", LINK),
        InfoItem("BMA Website", R.drawable.ic_baseline_bookmark_24, "https://608cecba944e0.site123.me/", LINK),
    )

    private lateinit var infoAdapter: InfoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoAdapter = InfoAdapter {
            if(it.type == EMAIL) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    type = "text/plain"
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, it.url)
                }
                if(intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    snackbar(R.string.error_no_email_apps)
                }
            } else {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(it.url)
                }
                if(intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    snackbar(R.string.error_no_browser_apps)
                }
            }
        }
        binding.rvInfoItems.apply {
            adapter = infoAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }
        infoAdapter.submitList(infoItems)
    }
}