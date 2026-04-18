package com.aegis.aegispass.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordEntryDao {
    @Query("SELECT * FROM password_entries ORDER BY title ASC")
    fun getAllEntries(): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): PasswordEntry?

    @Query("SELECT * FROM password_entries WHERE url = :url")
    fun getEntriesByUrl(url: String): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM password_entries WHERE title LIKE :query ORDER BY title ASC")
    fun searchEntries(query: String): Flow<List<PasswordEntry>>

    @Insert
    suspend fun insertEntry(entry: PasswordEntry): Long

    @Update
    suspend fun updateEntry(entry: PasswordEntry)

    @Delete
    suspend fun deleteEntry(entry: PasswordEntry)

    @Query("DELETE FROM password_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)

    @Query("SELECT COUNT(*) FROM password_entries")
    suspend fun getEntryCount(): Int
}
