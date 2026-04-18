package com.aegis.aegispass.totp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base32
import kotlin.math.pow
import kotlin.text.iterator

class TOTPGenerator {
    companion object {
        private const val TIME_STEP = 30L  // seconds
        private const val DIGITS = 6
    }

    fun generateCode(secret: String): String {
        return try {
            val decodedSecret = decodeBase32(secret)
            val counter = System.currentTimeMillis() / 1000 / TIME_STEP

            val hmac = generateHMAC(decodedSecret, counter)
            val code = extractCode(hmac)

            code.toString().padStart(DIGITS, '0')
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate TOTP code", e)
        }
    }

    fun generateCodeWithTime(secret: String, timeMs: Long): String {
        return try {
            val decodedSecret = decodeBase32(secret)
            val counter = timeMs / 1000 / TIME_STEP

            val hmac = generateHMAC(decodedSecret, counter)
            val code = extractCode(hmac)

            code.toString().padStart(DIGITS, '0')
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate TOTP code", e)
        }
    }

    fun getRemainingSeconds(): Int {
        val current = System.currentTimeMillis() / 1000
        return (TIME_STEP - (current % TIME_STEP)).toInt()
    }

    private fun generateHMAC(secret: ByteArray, counter: Long): ByteArray {
        val msg = ByteArray(8)
        var counterValue = counter
        for (i in 7 downTo 0) {
            msg[i] = (counterValue and 0xff).toByte()
            counterValue = counterValue shr 8
        }

        val hmac = Mac.getInstance("HmacSHA1")
        hmac.init(SecretKeySpec(secret, "HmacSHA1"))
        return hmac.doFinal(msg)
    }

    private fun extractCode(hmac: ByteArray): Int {
        val offset = (hmac[hmac.size - 1].toInt() and 0xf)
        val code = (hmac[offset].toInt() and 0x7f shl 24 or
                (hmac[offset + 1].toInt() and 0xff shl 16) or
                (hmac[offset + 2].toInt() and 0xff shl 8) or
                (hmac[offset + 3].toInt() and 0xff))

        return (code % 10.0.pow(DIGITS.toDouble())).toInt()
    }

    private fun decodeBase32(encoded: String): ByteArray {
        // Simple Base32 decoder - handles standard RFC 4648 alphabet
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val bits = mutableListOf<Boolean>()

        for (char in encoded.uppercase()) {
            if (char == '=') break
            val value = alphabet.indexOf(char)
            if (value < 0) throw IllegalArgumentException("Invalid Base32 character: $char")

            for (i in 4 downTo 0) {
                bits.add((value shr i) and 1 == 1)
            }
        }

        val bytes = mutableListOf<Byte>()
        for (i in 0 until bits.size / 8) {
            var byte = 0
            for (j in 0..7) {
                byte = (byte shl 1) or (if (bits[i * 8 + j]) 1 else 0)
            }
            bytes.add(byte.toByte())
        }

        return bytes.toByteArray()
    }
}
