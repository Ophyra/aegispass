package com.aegis.aegispass.crypto

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBKDFKeySpec
import java.security.SecureRandom
import java.util.Base64

class MasterPassword {
    companion object {
        private const val ITERATION_COUNT = 100000
        private const val KEY_LENGTH = 256  // bits
        private const val SALT_LENGTH = 16  // bytes
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    }

    fun deriveKey(password: String, salt: ByteArray = generateSalt()): Pair<ByteArray, String> {
        val spec = PBKDFKeySpec(
            password.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val derivedKey = factory.generateSecret(spec).encoded

        // Return both the key and the salt (encoded) so we can store the salt
        val encodedSalt = Base64.getEncoder().encodeToString(salt)
        return Pair(derivedKey, encodedSalt)
    }

    fun deriveKeyWithExistingSalt(password: String, encodedSalt: String): ByteArray {
        val salt = Base64.getDecoder().decode(encodedSalt)
        return deriveKey(password, salt).first
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String): String {
        val (derivedKey, salt) = deriveKey(password)
        val encodedKey = Base64.getEncoder().encodeToString(derivedKey)
        // Store as "salt:hash" so we can extract salt for verification
        return "$salt:$encodedKey"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = parts[0]
            val derivedKey = deriveKeyWithExistingSalt(password, salt)
            val encodedKey = Base64.getEncoder().encodeToString(derivedKey)

            encodedKey == parts[1]
        } catch (e: Exception) {
            false
        }
    }
}
