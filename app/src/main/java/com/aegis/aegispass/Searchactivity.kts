package com.aegis.aegispass

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aegis.aegispass.database.PasswordEntry
import com.aegis.aegispass.database.PasswordRepository
import com.aegis.aegispass.ui.PasswordEntryAdapter
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var repository: PasswordRepository
    private lateinit var adapter: PasswordEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        repository = PasswordRepository(this)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val recyclerView = findViewById<RecyclerView>(R.id.searchResultsRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PasswordEntryAdapter { entry ->
            openEntryDetails(entry)
        }
        recyclerView.adapter = adapter

        // Search on input change
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    adapter.submitList(emptyList())
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        backButton.setOnClickListener {
            finish()
        }

        // Auto-focus search input
        searchInput.requestFocus()
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            repository.searchEntries(query).collect { results ->
                adapter.submitList(results)
            }
        }
    }

    private fun openEntryDetails(entry: PasswordEntry) {
        val intent = Intent(this, EntryDetailsActivity::class.java)
        intent.putExtra("entry_id", entry.id)
        startActivity(intent)
    }
}