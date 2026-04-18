package com.aegis.aegispass.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "vault_prefs"
        private const val KEY_MASTER_PASSWORD_HASH = "master_password_hash"
        private const val KEY_IS_UNLOCKED = "is_unlocked"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"  // in milliseconds
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setMasterPasswordHash(hash: String) {
        prefs.edit().putString(KEY_MASTER_PASSWORD_HASH, hash).apply()
    }

    fun getMasterPasswordHash(): String? {
        return prefs.getString(KEY_MASTER_PASSWORD_HASH, null)
    }

    fun isMasterPasswordSet(): Boolean {
        return getMasterPasswordHash() != null
    }

    fun setUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_UNLOCKED, unlocked).apply()
    }

    fun isUnlocked(): Boolean {
        return prefs.getBoolean(KEY_IS_UNLOCKED, false)
    }

    fun setAutoLockTimeout(timeoutMs: Long) {
        prefs.edit().putLong(KEY_AUTO_LOCK_TIMEOUT, timeoutMs).apply()
    }

    fun getAutoLockTimeout(): Long {
        return prefs.getLong(KEY_AUTO_LOCK_TIMEOUT, 5 * 60 * 1000)  // Default 5 minutes
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
