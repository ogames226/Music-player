package com.example.model

enum class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceText: String,
    val periodText: String,
    val isBestValue: Boolean = false,
    val savingsText: String? = null
) {
    MONTHLY(
        id = "vip_monthly_499",
        title = "Monthly VIP",
        priceText = "$4.99",
        periodText = "/ month",
        isBestValue = false
    ),
    YEARLY(
        id = "vip_yearly_3999",
        title = "Annual VIP Pro",
        priceText = "$39.99",
        periodText = "/ year",
        isBestValue = true,
        savingsText = "Save 35%"
    ),
    LIFETIME(
        id = "vip_lifetime_8999",
        title = "Lifetime VIP Pass",
        priceText = "$89.99",
        periodText = "one-time",
        isBestValue = false,
        savingsText = "Pay Once, Forever VIP"
    )
}

data class VipStatus(
    val isVip: Boolean = false,
    val plan: SubscriptionPlan? = null,
    val expiresAtMs: Long? = null,
    val is24HourRewardActive: Boolean = false,
    val paymentGatewayUsed: String? = null, // "Stripe", "Moyasar", "GooglePay", "ApplePay"
    val transactionId: String? = null
) {
    val isAdFree: Boolean
        get() = isVip || is24HourRewardActive

    val isProEqualizerUnlocked: Boolean
        get() = isVip || is24HourRewardActive

    val isHqFlacUnlocked: Boolean
        get() = isVip || is24HourRewardActive

    val isProThemesUnlocked: Boolean
        get() = isVip

    val remainingRewardHours: Int
        get() {
            if (!is24HourRewardActive || expiresAtMs == null) return 0
            val diffMs = expiresAtMs - System.currentTimeMillis()
            return if (diffMs > 0) (diffMs / (1000 * 60 * 60)).toInt() else 0
        }
}
