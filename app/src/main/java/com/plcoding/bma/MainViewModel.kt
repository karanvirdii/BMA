package com.plcoding.bma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    private val _loadedAd = MutableStateFlow<InterstitialAd?>(null)
    val loadedAd: StateFlow<InterstitialAd?> = _loadedAd

    private val _addNoteEvent = MutableSharedFlow<Unit>()
    val addNoteEvent: SharedFlow<Unit> = _addNoteEvent

    private val _textIntentHandled = MutableStateFlow(false)
    val textIntentHandled: StateFlow<Boolean> = _textIntentHandled

    private val _shouldShowAds = MutableStateFlow(true)
    val shouldShowAds = _shouldShowAds.asStateFlow()

    fun sendAddNoteEvent() {
        viewModelScope.launch {
            _addNoteEvent.emit(Unit)
        }
    }

    fun setShowAds(shouldShow: Boolean) {
        _shouldShowAds.value = shouldShow
    }

    fun handleTextIntent() {
        _textIntentHandled.value = true
    }

    fun setLoadedAd(ad: InterstitialAd) {
        _loadedAd.value = ad
    }
}