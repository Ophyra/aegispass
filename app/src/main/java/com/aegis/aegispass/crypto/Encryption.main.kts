package com.aegis.aegispass.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.SecureRandom
import java.util.Base64

class Encryption {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "PasswordVaultKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParams = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParams)
        keyGenerator.generateKey()
    }

    fun encryptPassword(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        // Generate random IV
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)

        // Initialize cipher with IV
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        // Encrypt
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine IV + ciphertext and encode to Base64
        val combined = iv + ciphertext
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decryptPassword(encrypted: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            // Decode Base64
            val combined = Base64.getDecoder().decode(encrypted)

            // Extract IV and ciphertext
            val iv = combined.sliceArray(0 until IV_LENGTH)
            val ciphertext = combined.sliceArray(IV_LENGTH until combined.size)

            // Initialize cipher with IV
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            // Decrypt
            val plaintext = cipher.doFinal(ciphertext)
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }
}