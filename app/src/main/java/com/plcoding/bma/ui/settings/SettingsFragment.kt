package com.plcoding.bma.ui.settings

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.android.billingclient.api.*
import com.plcoding.bma.R
import com.plcoding.bma.databinding.*
import com.plcoding.bma.ui.BindingFragment
import com.plcoding.bma.ui.MainActivity
import com.plcoding.bma.util.Constants
import com.plcoding.bma.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPref: SharedPreferences
    private var appThemePref: ListPreference? = null
    private var adRemovalPref: Preference? = null

    private var billingClient: BillingClient? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        appThemePref = findPreference("app_theme")
        adRemovalPref = findPreference("ad_removal")

        val appThemeValue = sharedPref.getString(Constants.SHARED_PREF_KEY_APP_THEME, "0")
        appThemePref?.value = appThemeValue


        appThemePref?.setOnPreferenceChangeListener { preference, newValue ->
            sharedPref.edit()
                .putString(Constants.SHARED_PREF_KEY_APP_THEME, newValue.toString())
                .apply()
            val theme = when(newValue.toString().toIntOrNull()) {
                0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(theme)
            true
        }

        billingClient = (requireActivity() as? MainActivity)?.billingClient

        adRemovalPref?.setOnPreferenceClickListener { pref ->
            lifecycleScope.launch {
                querySkuDetails()
            }
            true
        }
    }

    private suspend fun querySkuDetails() {
        val skuList = arrayListOf<String>()
        skuList.add("ad_removal")
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient?.querySkuDetails(params)
        }
        skuDetailsResult?.let { result ->
            Timber.d("Sku details result: ${result.billingResult.responseCode}")
            if(result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = result.skuDetailsList?.firstOrNull()
                details?.let {
                    launchPurchaseFlow(details)
                } ?: snackbar(R.string.error_unknown)
            }
        }
    }

    private fun launchPurchaseFlow(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient?.launchBillingFlow(requireActivity(), flowParams)?.responseCode
        if(responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("Purchase flow successfully started")
        } else {
            Timber.d("Purchase flow couldn't be launched. Error code: $responseCode")
        }
    }
}