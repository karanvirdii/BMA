package com.plcoding.bma.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.util.Patterns.WEB_URL
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.BuildConfig
import com.plcoding.bma.MainViewModel
import com.plcoding.bma.R
import com.plcoding.bma.data.models.Folder
import com.plcoding.bma.data.models.Note
import com.plcoding.bma.databinding.ActivityMainBinding
import com.plcoding.bma.ui.addnote.AddNoteFragmentDirections
import com.plcoding.bma.util.Constants
import com.plcoding.bma.util.Constants.KEY_SHOW_ADS
import com.plcoding.bma.util.Constants.SKU_AD_REMOVAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PurchasesUpdatedListener, BillingClientStateListener {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment: NavHostFragment

    private val showBottomNavIn = listOf<Int>(
        R.id.homeFragment,
        R.id.favoritesFragment,
        R.id.historyFragment,
        R.id.noteDetailFragment
    )
    private val showBackArrowIn = listOf(
        R.id.noteDetailFragment,
        R.id.addNoteFragment,
        R.id.infoFragment,
        R.id.settingsFragment,
        R.id.addFolderFragment
    )

    private val viewModel: MainViewModel by viewModels()

    val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setShowAds(shouldShowAds())

        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)

        // Needed to prevent reloading of items when reselecting bottom nav item item
        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.isVisible = destination.id in showBottomNavIn
            val shouldShowBackArrow = destination.id in showBackArrowIn
            supportActionBar?.setDisplayShowHomeEnabled(shouldShowBackArrow)
            supportActionBar?.setDisplayHomeAsUpEnabled(shouldShowBackArrow)
        }

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" && !viewModel.textIntentHandled.value) {
            handleTextIntent(intent)
            viewModel.handleTextIntent()
        }

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            Constants.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    viewModel.setLoadedAd(ad)
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    super.onAdFailedToLoad(err)
                    Timber.d("AD FAILED TO LOAD: ${err.message}")
                }
            })

        billingClient.startConnection(this)
    }

    override fun onBillingServiceDisconnected() {
        Timber.d("Billing service disconnected")
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if(result.responseCode == BillingClient.BillingResponseCode.OK) {
            lifecycleScope.launch {
                checkForNewPurchases()
            }
        } else {
            Timber.d("Billing client setup failed with response code ${result.responseCode}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when(result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if(purchases != null) {
                    lifecycleScope.launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                } else if(purchases?.isEmpty() == true) {
                    setShouldShowAds(true)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                snackbar(R.string.purchase_canceled)
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->{
                billingClient.startConnection(this)
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                snackbar(R.string.billing_unavailable)
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                snackbar(R.string.service_unavailable)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Unit
            else -> {
                snackbar(R.string.purchase_failed)
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if(!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                }
            }
            if (purchase.skus.contains(SKU_AD_REMOVAL)) {
                Timber.d("Purchase was successful")
                setShouldShowAds(false)
            }
        } else if(purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            snackbar(R.string.pending_purchase)
            setShouldShowAds(true)
        } else {
            setShouldShowAds(true)
        }
        Timber.d("Handling purchase with state ${purchase.purchaseState}")
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            checkForNewPurchases()
        }
    }

    private suspend fun checkForNewPurchases() {
        withContext(Dispatchers.IO) {
            val result = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
            when (result.billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Timber.d("There were new purchases: ${result.purchasesList}")
                    if(result.purchasesList.isEmpty()) {
                        setShouldShowAds(true)
                        return@withContext
                    }
                    lifecycleScope.launch {
                        for (purchase in result.purchasesList) {
                            handlePurchase(purchase)
                        }
                    }
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Timber.d("Service is disconnected.")
                    billingClient.startConnection(this@MainActivity)
                }
                else -> {
                    Timber.d("Check for new purchases returned an error: ${result.billingResult.responseCode}")
                }
            }
        }
    }

    private fun handleTextIntent(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { url ->
            if (!WEB_URL.matcher(url).matches()) {
                snackbar(getString(R.string.error_invalid_url))
                return
            }
            navHostFragment.findNavController().navigate(
                AddNoteFragmentDirections.globalActionToAddNoteFragment(
                    Folder(
                        "Internet",
                        R.drawable.ic_baseline_web_64
                    ),
                    Note(
                        "",
                        url,
                        "Internet",
                        resources.getResourceEntryName(R.drawable.ic_baseline_web_64)
                    )
                )
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleTextIntent(intent)
        }
    }

    fun switchToBottomNavTab(index: Int) {
        binding.bottomNavigationView.selectedItemId = index
    }

    private fun snackbar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).apply {
            anchorView = binding.bottomNavigationView
        }.show()
    }

    private fun snackbar(@StringRes stringRes: Int) {
        Snackbar.make(binding.root, stringRes, Snackbar.LENGTH_LONG).apply {
            anchorView = binding.bottomNavigationView
        }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp()
    }

    private fun shouldShowAds() = sharedPref.getBoolean(KEY_SHOW_ADS, true)

    private fun setShouldShowAds(shouldShow: Boolean) {
        sharedPref.edit()
            .putBoolean(KEY_SHOW_ADS, shouldShow)
            .apply()
        viewModel.setShowAds(shouldShow)
    }

}