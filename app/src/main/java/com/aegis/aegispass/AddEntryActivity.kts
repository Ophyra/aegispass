package com.aegis.aegispass

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aegis.aegispass.crypto.Encryption
import com.aegis.aegispass.database.PasswordEntry
import com.aegis.aegispass.database.VaultDatabase
import com.aegis.aegispass.password.PasswordGenerator
import kotlinx.coroutines.launch

class AddEntryActivity : AppCompatActivity() {
    private lateinit var database: VaultDatabase
    private lateinit var encryption: Encryption
    private lateinit var passwordGenerator: PasswordGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)

        database = VaultDatabase.getDatabase(this)
        encryption = Encryption()
        passwordGenerator = PasswordGenerator()

        val titleInput = findViewById<EditText>(R.id.titleInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val totpInput = findViewById<EditText>(R.id.totpInput)
        val notesInput = findViewById<EditText>(R.id.notesInput)

        val generatePasswordButton = findViewById<Button>(R.id.generatePasswordButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        generatePasswordButton.setOnClickListener {
            val generated = passwordGenerator.generateStrong(16)
            passwordInput.setText(generated)
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val url = urlInput.text.toString()
            val totp = totpInput.text.toString()
            val notes = notesInput.text.toString()

            if (title.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Title, username, and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val encryptedPassword = encryption.encryptPassword(password)
                    val encryptedTotp = if (totp.isNotEmpty()) encryption.encryptPassword(totp) else null

                    val entry = PasswordEntry(
                        title = title,
                        username = username,
                        encryptedPassword = encryptedPassword,
                        url = url.ifEmpty { null },
                        totp_secret = encryptedTotp,
                        notes = notes.ifEmpty { null }
                    )

                    database.passwordEntryDao().insertEntry(entry)
                    Toast.makeText(this@AddEntryActivity, "Entry saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@AddEntryActivity, "Error saving entry: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}
