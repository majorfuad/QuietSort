package com.fuad.quietsort.classify

import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartClassifierTest {
    @Test fun offlineCategories() {
        assertEquals("Messages", SmartClassifier.classify("com.whatsapp", "Hello", "Test", null))
        assertEquals("Finance", SmartClassifier.classify("com.example.app", "Bank", "Transaction debited", null))
        assertEquals("Shopping", SmartClassifier.classify("com.example.app", "Your order", "Shipped", null))
        assertEquals("Promotions", SmartClassifier.classify("com.example.app", "Coupon", "Sale offer", null))
        assertEquals("Other", SmartClassifier.classify("com.example.app", null, null, null))
    }

    @Test fun priorityRuleOverridesBuiltInCategory() {
        val rules = listOf(
            ClassificationRuleEntity(
                id = 2, name = "Shopping override", matchType = "APP",
                pattern = "com.whatsapp", targetCategory = "Shopping", priority = 10
            )
        )
        assertEquals("Shopping", SmartClassifier.classify("com.whatsapp", "Hello", null, null, rules))
    }

    @Test fun disabledRuleDoesNotOverride() {
        val rules = listOf(
            ClassificationRuleEntity(
                name = "Disabled", matchType = "KEYWORD", pattern = "hello",
                targetCategory = "Finance", enabled = false
            )
        )
        assertEquals("Messages", SmartClassifier.classify("com.whatsapp", "Hello", null, null, rules))
    }
}
