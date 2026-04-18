package com.aegis.aegispass.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper

class ClipboardUtil(private val context: Context) {
    companion object {
        private const val CLEAR_DELAY_MS = 60000L  // 60 seconds
    }

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var clearHandler: Handler? = null

    fun copyToClipboard(label: String, text: String, autoClearAfterSeconds: Long = 60) {
        // Clear any pending clear operations
        clearHandler?.removeCallbacksAndMessages(null)

        // Copy to clipboard
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)

        // Schedule auto-clear
        if (autoClearAfterSeconds > 0) {
            clearHandler = Handler(Looper.getMainLooper())
            clearHandler?.postDelayed({
                clearClipboard()
            }, autoClearAfterSeconds * 1000)
        }
    }

    fun clearClipboard() {
        val clip = ClipData.newPlainText("", "")
        clipboardManager.setPrimaryClip(clip)
    }

    fun getClipboardText(): String? {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}