package com.example.vocanote.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Insert
    suspend fun insert(entry: VocabEntry)

    @Query("SELECT * FROM vocab_entries ORDER BY createdAt DESC")
    fun getAll(): Flow<List<VocabEntry>>

    @Delete
    suspend fun delete(entry: VocabEntry)

    @Query("DELETE FROM vocab_entries WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)
}
