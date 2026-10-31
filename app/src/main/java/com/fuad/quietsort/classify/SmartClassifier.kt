package com.fuad.quietsort.classify

import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity

/** Fully deterministic, offline classification. No ML download/network required. */
object SmartClassifier {
    val categories = listOf("Messages", "Finance", "Shopping", "Promotions", "Social", "Work", "System", "Other")

    fun classify(
        packageName: String,
        title: String?,
        body: String?,
        androidCategory: String?,
        rules: List<ClassificationRuleEntity> = emptyList()
    ): String {
        val searchable = listOfNotNull(title, body).joinToString(" ").lowercase()
        rules.asSequence().filter { it.enabled && it.targetCategory in categories }
            .sortedWith(compareBy<ClassificationRuleEntity> { it.priority }.thenBy { it.id })
            .firstOrNull { rule ->
                val value = rule.pattern.trim().lowercase()
                value.isNotEmpty() && when (rule.matchType) {
                    "APP" -> packageName.equals(value, ignoreCase = true)
                    "KEYWORD" -> value in searchable
                    "TITLE" -> title?.contains(value, ignoreCase = true) == true
                    else -> false
                }
            }?.let { return it.targetCategory }

        val pkg = packageName.lowercase()
        val content = searchable
        return when {
            androidCategory in listOf("msg", "message") || listOf("whatsapp", "telegram", "signal", "messenger", "sms").any { it in pkg } -> "Messages"
            listOf("bank", "payment", "wallet", "transaction", "debited", "credited", "bKash", "nagad", "otp").any { it.lowercase() in content } -> "Finance"
            listOf("order", "delivery", "shipped", "parcel", "tracking").any { it in content } -> "Shopping"
            listOf("discount", "promo", "sale", "coupon", "offer", "deal").any { it in content } -> "Promotions"
            listOf("instagram", "facebook", "reddit", "twitter", "tiktok").any { it in pkg } -> "Social"
            listOf("calendar", "meeting", "teams", "slack", "outlook").any { it in pkg } -> "Work"
            androidCategory in listOf("sys", "service", "status", "progress") ||
                pkg.startsWith("com.android.") || pkg.startsWith("com.samsung.android.") -> "System"
            else -> "Other"
        }
    }
}
