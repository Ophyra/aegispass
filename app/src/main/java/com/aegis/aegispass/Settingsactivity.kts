package com.aegis.aegispass

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.aegis.aegispass.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefManager = PreferenceManager(this)

        val autoLockSwitch = findViewById<Switch>(R.id.autoLockSwitch)
        val autoLockTimeoutSeekBar = findViewById<SeekBar>(R.id.autoLockTimeoutSeekBar)
        val autoLockTimeoutLabel = findViewById<TextView>(R.id.autoLockTimeoutLabel)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Load current auto-lock timeout
        val currentTimeout = prefManager.getAutoLockTimeout()
        autoLockSwitch.isChecked = currentTimeout > 0
        autoLockTimeoutSeekBar.progress = (currentTimeout / 60000).toInt().coerceIn(1, 30)
        updateTimeoutLabel(autoLockTimeoutLabel, autoLockTimeoutSeekBar.progress)

        autoLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            autoLockTimeoutSeekBar.isEnabled = isChecked
            if (isChecked) {
                val timeoutMs = autoLockTimeoutSeekBar.progress * 60000L
                prefManager.setAutoLockTimeout(timeoutMs)
            } else {
                prefManager.setAutoLockTimeout(0)
            }
        }

        autoLockTimeoutSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateTimeoutLabel(autoLockTimeoutLabel, progress)
                    val timeoutMs = progress * 60000L
                    prefManager.setAutoLockTimeout(timeoutMs)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        resetButton.setOnClickListener {
            showResetConfirmation()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateTimeoutLabel(label: TextView, minutes: Int) {
        label.text = "Auto-lock after: $minutes minute${if (minutes > 1) "s" else ""}"
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Data")
            .setMessage("This will permanently delete all passwords and settings. This cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        prefManager.clear()
        Toast.makeText(this, "All data has been reset", Toast.LENGTH_SHORT).show()

        // Return to login screen
        val intent = android.content.Intent(this, MainActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}