package com.aegis.aegispass

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aegis.aegispass.crypto.Encryption
import com.aegis.aegispass.database.VaultDatabase
import com.aegis.aegispass.totp.TOTPGenerator
import kotlinx.coroutines.launch

class EntryDetailsActivity : AppCompatActivity() {
    private lateinit var database: VaultDatabase
    private lateinit var encryption: Encryption
    private lateinit var totpGenerator: TOTPGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_details)

        database = VaultDatabase.getDatabase(this)
        encryption = Encryption()
        totpGenerator = TOTPGenerator()

        val entryId = intent.getIntExtra("entry_id", -1)
        if (entryId == -1) {
            finish()
            return
        }

        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val passwordTextView = findViewById<TextView>(R.id.passwordTextView)
        val urlTextView = findViewById<TextView>(R.id.urlTextView)
        val totpTextView = findViewById<TextView>(R.id.totpTextView)
        val notesTextView = findViewById<TextView>(R.id.notesTextView)

        val copyUsernameButton = findViewById<Button>(R.id.copyUsernameButton)
        val copyPasswordButton = findViewById<Button>(R.id.copyPasswordButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)

        lifecycleScope.launch {
            val entry = database.passwordEntryDao().getEntryById(entryId)
            if (entry != null) {
                val decryptedPassword = encryption.decryptPassword(entry.encryptedPassword)

                titleTextView.text = entry.title
                usernameTextView.text = entry.username
                passwordTextView.text = decryptedPassword
                urlTextView.text = entry.url ?: "N/A"
                notesTextView.text = entry.notes ?: "N/A"

                if (entry.totp_secret != null) {
                    try {
                        val decryptedTotp = encryption.decryptPassword(entry.totp_secret)
                        val code = totpGenerator.generateCode(decryptedTotp)
                        val remaining = totpGenerator.getRemainingSeconds()
                        totpTextView.text = "$code (expires in ${remaining}s)"
                    } catch (e: Exception) {
                        totpTextView.text = "Error decoding TOTP"
                    }
                } else {
                    totpTextView.text = "N/A"
                }

                copyUsernameButton.setOnClickListener {
                    copyToClipboard("Username", entry.username)
                }

                copyPasswordButton.setOnClickListener {
                    copyToClipboard("Password", decryptedPassword)
                }

                deleteButton.setOnClickListener {
                    lifecycleScope.launch {
                        database.passwordEntryDao().deleteEntry(entry)
                        Toast.makeText(this@EntryDetailsActivity, "Entry deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
