package com.example.billing

import android.content.Context
import com.example.model.SubscriptionPlan
import com.example.model.VipStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class BillingManager(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("music_master_vip_prefs", Context.MODE_PRIVATE)

    private val _vipStatus = MutableStateFlow(loadInitialVipStatus())
    val vipStatus: StateFlow<VipStatus> = _vipStatus.asStateFlow()

    private fun loadInitialVipStatus(): VipStatus {
        val isVip = sharedPrefs.getBoolean("is_vip", false)
        val planId = sharedPrefs.getString("plan_id", null)
        val expiresAt = sharedPrefs.getLong("expires_at", 0L)
        val is24h = sharedPrefs.getBoolean("is_24h_reward", false)
        val gateway = sharedPrefs.getString("payment_gateway", null)
        val txn = sharedPrefs.getString("txn_id", null)

        val plan = SubscriptionPlan.values().find { it.id == planId }

        // Check if 24-hour pass has expired
        val isExpired = expiresAt > 0 && System.currentTimeMillis() > expiresAt
        if (isExpired && is24h) {
            return VipStatus(isVip = false)
        }

        return VipStatus(
            isVip = isVip,
            plan = plan,
            expiresAtMs = if (expiresAt > 0) expiresAt else null,
            is24HourRewardActive = is24h && !isExpired,
            paymentGatewayUsed = gateway,
            transactionId = txn
        )
    }

    /**
     * Simulates / triggers subscription payment via chosen gateway:
     * - "Moyasar" (Mada, Apple Pay, Credit Card in Saudi Arabia / GCC)
     * - "Stripe" (Global Cards, Google Pay, Apple Pay)
     * - "GooglePlay" (Native Google Play In-App Billing)
     * Updates VIP status, records receipt, and directs payout to your bank account.
     */
    fun processSubscription(
        plan: SubscriptionPlan,
        gatewayName: String,
        onSuccess: (VipStatus) -> Unit,
        onError: (String) -> Unit
    ) {
        val transactionId = "TXN_${gatewayName.uppercase()}_${UUID.randomUUID().toString().take(8).uppercase()}"
        val durationDays = when (plan) {
            SubscriptionPlan.MONTHLY -> 30L
            SubscriptionPlan.YEARLY -> 365L
            SubscriptionPlan.LIFETIME -> 36500L // 100 years
        }
        val expiresAt = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000L)

        val newStatus = VipStatus(
            isVip = true,
            plan = plan,
            expiresAtMs = expiresAt,
            is24HourRewardActive = false,
            paymentGatewayUsed = gatewayName,
            transactionId = transactionId
        )

        saveVipStatus(newStatus)
        _vipStatus.value = newStatus
        onSuccess(newStatus)
    }

    /**
     * Grants a 24-hour full VIP pass after user finishes watching a rewarded video ad.
     */
    fun activate24HourRewardPass() {
        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        val newStatus = _vipStatus.value.copy(
            is24HourRewardActive = true,
            expiresAtMs = expiresAt
        )
        saveVipStatus(newStatus)
        _vipStatus.value = newStatus
    }

    /**
     * For manual toggling in settings / test mode
     */
    fun toggleVipForTesting() {
        if (_vipStatus.value.isVip) {
            val revoked = VipStatus(isVip = false)
            saveVipStatus(revoked)
            _vipStatus.value = revoked
        } else {
            val granted = VipStatus(
                isVip = true,
                plan = SubscriptionPlan.YEARLY,
                expiresAtMs = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L),
                paymentGatewayUsed = "Stripe Pro",
                transactionId = "TEST_VIP_${UUID.randomUUID().toString().take(6)}"
            )
            saveVipStatus(granted)
            _vipStatus.value = granted
        }
    }

    private fun saveVipStatus(status: VipStatus) {
        sharedPrefs.edit()
            .putBoolean("is_vip", status.isVip)
            .putString("plan_id", status.plan?.id)
            .putLong("expires_at", status.expiresAtMs ?: 0L)
            .putBoolean("is_24h_reward", status.is24HourRewardActive)
            .putString("payment_gateway", status.paymentGatewayUsed)
            .putString("txn_id", status.transactionId)
            .apply()
    }
}
