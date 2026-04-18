package com.aegis.aegispass.autofill

import android.content.Context
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillValue
import com.aegis.aegispass.crypto.Encryption
import com.aegis.aegispass.database.PasswordEntry

class AutofillResponseBuilder(private val context: Context) {
    private val datasets = mutableListOf<Dataset>()

    fun addDatasets(
        entries: List<PasswordEntry>,
        fields: List<AutofillField>,
        encryption: Encryption
    ): AutofillResponseBuilder {
        for (entry in entries) {
            try {
                val decryptedPassword = encryption.decryptPassword(entry.encryptedPassword)
                val dataset = buildDataset(entry, decryptedPassword, fields)
                if (dataset != null) {
                    datasets.add(dataset)
                }
            } catch (e: Exception) {
                // Skip entries that fail to decrypt
                continue
            }
        }
        return this
    }

    private fun buildDataset(
        entry: PasswordEntry,
        decryptedPassword: String,
        fields: List<AutofillField>
    ): Dataset? {
        if (fields.isEmpty()) return null

        val builder = Dataset.Builder()
        builder.setAuthentication(null)

        // Find username and password fields
        var usernameFieldFound = false
        var passwordFieldFound = false

        for (field in fields) {
            val hints = field.autofillHints
            val hint = field.hint?.lowercase() ?: ""

            if ((hints != null && hints.any { it.contains("username", ignoreCase = true) || it.contains("email", ignoreCase = true) }) ||
                hint.contains("username") || hint.contains("email")) {
                builder.setField(field.autofillId, AutofillValue.forText(entry.username))
                usernameFieldFound = true
            } else if ((hints != null && hints.any { it.contains("password", ignoreCase = true) }) ||
                hint.contains("password")) {
                builder.setField(field.autofillId, AutofillValue.forText(decryptedPassword))
                passwordFieldFound = true
            }
        }

        // Only return dataset if we found at least password field
        return if (passwordFieldFound) {
            builder.build()
        } else {
            null
        }
    }

    fun build(): FillResponse? {
        if (datasets.isEmpty()) return null

        return FillResponse.Builder()
            .addDatasets(datasets)
            .build()
    }
}