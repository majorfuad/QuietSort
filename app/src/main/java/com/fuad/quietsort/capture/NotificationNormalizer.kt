package com.fuad.quietsort.capture

object NotificationNormalizer {
    private const val MAX_TEXT_CHARS = 4096

    fun normalize(input: NotificationPayload): NormalizedNotification? {
        val key = input.key?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val pkg = input.packageName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return NormalizedNotification(
            systemKey = key,
            packageName = pkg,
            notificationId = input.id,
            tag = input.tag,
            postedAtMs = input.postTime.coerceAtLeast(0L),
            title = sanitize(input.title),
            text = sanitize(input.text),
            bigText = sanitize(input.bigText),
            conversationTitle = sanitize(input.conversationTitle),
            category = input.category?.trim()?.takeIf { it.isNotEmpty() },
            channelId = input.channelId?.trim()?.takeIf { it.isNotEmpty() },
            isOngoing = input.ongoing,
            isClearable = input.clearable,
            groupKey = input.groupKey?.trim()?.takeIf { it.isNotEmpty() },
            isGroupSummary = input.groupSummary
        )
    }

    fun sanitize(value: CharSequence?): String? {
        return value?.toString()
            ?.replace(Regex("[\\p{Cntrl}&&[^\\n\\t]]"), " ")
            ?.trim()
            ?.take(MAX_TEXT_CHARS)
            ?.takeIf { it.isNotEmpty() }
    }
}
