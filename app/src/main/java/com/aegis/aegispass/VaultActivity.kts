package com.aegis.aegispass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aegis.aegispass.database.PasswordEntry
import com.aegis.aegispass.database.VaultDatabase
import com.aegis.aegispass.ui.PasswordEntryAdapter
import com.aegis.aegispass.utils.PreferenceManager
import kotlinx.coroutines.launch

class VaultActivity : AppCompatActivity() {
    private lateinit var database: VaultDatabase
    private lateinit var prefManager: PreferenceManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PasswordEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault)

        database = VaultDatabase.getDatabase(this)
        prefManager = PreferenceManager(this)

        // Check if user is unlocked
        if (!prefManager.isUnlocked()) {
            navigateToLogin()
            return
        }

        recyclerView = findViewById(R.id.entriesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PasswordEntryAdapter { entry ->
            openEntryDetails(entry)
        }
        recyclerView.adapter = adapter

        val addButton = findViewById<Button>(R.id.addButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val lockButton = findViewById<Button>(R.id.lockButton)

        addButton.setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        lockButton.setOnClickListener {
            prefManager.setUnlocked(false)
            navigateToLogin()
        }

        loadEntries()
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            database.passwordEntryDao().getAllEntries().collect { entries ->
                adapter.submitList(entries)
            }
        }
    }

    private fun openEntryDetails(entry: PasswordEntry) {
        val intent = Intent(this, EntryDetailsActivity::class.java)
        intent.putExtra("entry_id", entry.id)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!prefManager.isUnlocked()) {
            navigateToLogin()
        } else {
            loadEntries()
        }
    }
}