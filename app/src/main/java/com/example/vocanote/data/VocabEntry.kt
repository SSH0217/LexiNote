package com.example.vocanote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocab_entries")
data class VocabEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val sentence: String,
    val meaning: String? = null,
    val sentenceTranslation: String? = null,
    val groupId: String,
    val createdAt: Long = System.currentTimeMillis()
)
