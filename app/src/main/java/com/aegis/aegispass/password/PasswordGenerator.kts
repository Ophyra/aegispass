package com.aegis.aegispass.password

import java.security.SecureRandom

class PasswordGenerator {
    companion object {
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val DIGITS = "0123456789"
        private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"
    }

    private val random = SecureRandom()

    fun generate(
        length: Int = 16,
        includeLowercase: Boolean = true,
        includeUppercase: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val charset = buildString {
            if (includeLowercase) append(LOWERCASE)
            if (includeUppercase) append(UPPERCASE)
            if (includeDigits) append(DIGITS)
            if (includeSymbols) append(SYMBOLS)
        }

        if (charset.isEmpty()) {
            throw IllegalArgumentException("At least one character type must be enabled")
        }

        if (length < 1) {
            throw IllegalArgumentException("Password length must be at least 1")
        }

        return (1..length)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }

    fun generateStrong(length: Int = 16): String {
        // Strong password: uppercase, lowercase, digits, symbols
        return generate(
            length = length,
            includeLowercase = true,
            includeUppercase = true,
            includeDigits = true,
            includeSymbols = true
        )
    }

    fun generateMedium(length: Int = 12): String {
        // Medium password: uppercase, lowercase, digits (no symbols)
        return generate(
            length = length,
            includeLowercase = true,
            includeUppercase = true,
            includeDigits = true,
            includeSymbols = false
        )
    }

    fun generateSimple(length: Int = 10): String {
        // Simple password: lowercase, uppercase, digits
        return generate(
            length = length,
            includeLowercase = true,
            includeUppercase = true,
            includeDigits = true,
            includeSymbols = false
        )
    }
}
