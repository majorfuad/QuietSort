package com.fuad.quietsort.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "classification_rules", indices = [Index("priority")])
data class ClassificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val matchType: String,
    val pattern: String,
    val targetCategory: String,
    val priority: Int = 100,
    val enabled: Boolean = true
)
