package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.model.VipStatus
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdMobManager(private val context: Context) {

    private val tag = "AdMobManager"

    // Real AdMob IDs configured for Music Master VIP
    val appId = "ca-app-pub-1782857967313956~8656090807"
    val bannerAdUnitId = "ca-app-pub-1782857967313956/2323662927"
    val interstitialAdUnitId = "ca-app-pub-1782857967313956/6618578263"
    val rewardedAdUnitId = "ca-app-pub-1782857967313956/4918483789"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var songChangeCounter = 0
    private val songsBetweenInterstitials = 4

    private val _isShowingInterstitial = MutableStateFlow(false)
    val isShowingInterstitial: StateFlow<Boolean> = _isShowingInterstitial.asStateFlow()

    private val _isShowingRewarded = MutableStateFlow(false)
    val isShowingRewarded: StateFlow<Boolean> = _isShowingRewarded.asStateFlow()

    init {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(tag, "AdMob MobileAds initialized: $status")
            }
            loadInterstitialAd()
            loadRewardedAd()
        } catch (e: Exception) {
            Log.w(tag, "AdMob initialization error: ${e.message}")
        }
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(tag, "AdMob Interstitial loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.w(tag, "AdMob Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(tag, "AdMob Rewarded Ad loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(tag, "AdMob Rewarded failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Determines whether any AdMob ad should be requested or displayed.
     * VIP members and 24h reward pass holders are 100% ad-free!
     */
    fun shouldShowAds(vipStatus: VipStatus): Boolean {
        return !vipStatus.isAdFree
    }

    /**
     * Increments song counter and triggers an interstitial ad if conditions are met.
     * For VIP users, this immediately executes action without interruption.
     */
    fun onSongChanged(vipStatus: VipStatus, onAdDismissedOrBypassed: () -> Unit) {
        if (!shouldShowAds(vipStatus)) {
            Log.d(tag, "VIP active: Interstitial suppressed completely.")
            onAdDismissedOrBypassed()
            return
        }

        songChangeCounter++
        if (songChangeCounter >= songsBetweenInterstitials) {
            songChangeCounter = 0
            val activity = context as? Activity
            val ad = interstitialAd

            if (activity != null && ad != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                        onAdDismissedOrBypassed()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        interstitialAd = null
                        loadInterstitialAd()
                        onAdDismissedOrBypassed()
                    }
                }
                ad.show(activity)
            } else {
                // Show in-app fallback interstitial modal
                _isShowingInterstitial.value = true
            }
        } else {
            onAdDismissedOrBypassed()
        }
    }

    fun dismissInterstitial(onComplete: () -> Unit) {
        _isShowingInterstitial.value = false
        onComplete()
    }

    /**
     * Shows rewarded ad for 24-hour VIP pass.
     */
    fun showRewardedAd(activity: Activity?, onRewarded: () -> Unit) {
        val ad = rewardedAd
        if (activity != null && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    _isShowingRewarded.value = true // fallback to simulated modal
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(tag, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            _isShowingRewarded.value = true
        }
    }

    fun completeRewardedAd(onRewarded: () -> Unit) {
        _isShowingRewarded.value = false
        onRewarded()
    }

    fun dismissRewardedAd() {
        _isShowingRewarded.value = false
    }
}
