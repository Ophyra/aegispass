package com.aegis.aegispass.database

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PasswordRepository(context: Context) {
    private val database = VaultDatabase.getDatabase(context)
    private val entryDao = database.passwordEntryDao()

    // Retrieve all entries
    fun getAllEntries(): Flow<List<PasswordEntry>> = entryDao.getAllEntries()

    // Get single entry
    suspend fun getEntryById(id: Int): PasswordEntry? = entryDao.getEntryById(id)

    // Get entries by URL
    fun getEntriesByUrl(url: String): Flow<List<PasswordEntry>> = entryDao.getEntriesByUrl(url)

    // Search entries
    fun searchEntries(query: String): Flow<List<PasswordEntry>> = entryDao.searchEntries("%$query%")

    // Insert new entry
    suspend fun insertEntry(entry: PasswordEntry): Long = entryDao.insertEntry(entry)

    // Update entry
    suspend fun updateEntry(entry: PasswordEntry) = entryDao.updateEntry(entry)

    // Delete entry
    suspend fun deleteEntry(entry: PasswordEntry) = entryDao.deleteEntry(entry)

    // Delete entry by ID
    suspend fun deleteEntryById(id: Int) = entryDao.deleteEntryById(id)

    // Get entry count
    suspend fun getEntryCount(): Int = entryDao.getEntryCount()
}