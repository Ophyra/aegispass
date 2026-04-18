package com.aegis.aegispass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aegis.aegispass.crypto.MasterPassword
import com.aegis.aegispass.utils.PreferenceManager

class MainActivity : AppCompatActivity() {
    private lateinit var prefManager: PreferenceManager
    private lateinit var masterPassword: MasterPassword

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefManager = PreferenceManager(this)
        masterPassword = MasterPassword()

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val unlockButton = findViewById<Button>(R.id.unlockButton)

        unlockButton.setOnClickListener {
            val enteredPassword = passwordInput.text.toString()

            if (enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (prefManager.isMasterPasswordSet()) {
                // User has set a password before - verify it
                val storedHash = prefManager.getMasterPasswordHash()
                if (storedHash != null && masterPassword.verifyPassword(enteredPassword, storedHash)) {
                    prefManager.setUnlocked(true)
                    navigateToVault()
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    passwordInput.text.clear()
                }
            } else {
                // First time setup - set the master password
                val hash = masterPassword.hashPassword(enteredPassword)
                prefManager.setMasterPasswordHash(hash)
                prefManager.setUnlocked(true)
                Toast.makeText(this, "Master password set successfully", Toast.LENGTH_SHORT).show()
                navigateToVault()
            }
        }
    }

    private fun navigateToVault() {
        val intent = Intent(this, VaultActivity::class.java)
        startActivity(intent)
        finish()
    }
}
