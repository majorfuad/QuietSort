package com.fuad.quietsort.capture

import org.junit.Assert.*
import org.junit.Test

class NotificationNormalizerTest {
    private fun input(
        key: String? = "key",
        pkg: String? = "example.app",
        title: CharSequence? = " Hello ",
        text: CharSequence? = " Message "
    ) = NotificationPayload(
        key = key, packageName = pkg, id = 3, tag = null,
        postTime = 123L, title = title, text = text,
        bigText = null, conversationTitle = null, category = null,
        channelId = null, ongoing = false, clearable = true,
        groupKey = null, groupSummary = false
    )

    @Test fun trimsText() {
        val value = requireNotNull(NotificationNormalizer.normalize(input()))
        assertEquals("Hello", value.title)
        assertEquals("Message", value.text)
    }

    @Test fun preservesNullForRedactedValues() {
        val value = requireNotNull(NotificationNormalizer.normalize(input(title = null, text = null)))
        assertNull(value.title)
        assertNull(value.text)
    }

    @Test fun rejectsInvalidIdentity() {
        assertNull(NotificationNormalizer.normalize(input(key = "")))
        assertNull(NotificationNormalizer.normalize(input(pkg = " ")))
    }

    @Test fun capsSizeAndRemovesControlCharacters() {
        assertEquals(4096, NotificationNormalizer.sanitize("a".repeat(9999))?.length)
        assertEquals("a b", NotificationNormalizer.sanitize("a\u0000b"))
    }

    @Test fun maintainsIdentityAcrossUpdates() {
        val a = requireNotNull(NotificationNormalizer.normalize(input(text = "A")))
        val b = requireNotNull(NotificationNormalizer.normalize(input(text = "B")))
        assertEquals(a.systemKey, b.systemKey)
        assertNotEquals(a.text, b.text)
    }
}
