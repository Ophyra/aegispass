package com.aegis.aegispass.autofill

import android.app.assist.AssistStructure
import android.content.IntentSender
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.aegis.aegispass.crypto.Encryption
import com.aegis.aegispass.database.VaultDatabase
import kotlinx.coroutines.launch

class PasswordAutofillService : AutofillService() {
    companion object {
        private const val TAG = "PasswordAutofillService"
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        cancellationSignal.setOnCancelListener {
            Log.d(TAG, "Autofill request cancelled")
        }

        val context = this
        val fillContexts = request.fillContexts
        val structure = fillContexts[fillContexts.size - 1].structure

        val packageName = structure.packageName
        Log.d(TAG, "Autofill requested for package: $packageName")

        // Parse the structure to find username/password fields
        val parser = AutofillParser(structure)
        val fields = parser.parse()

        if (fields.isEmpty()) {
            Log.d(TAG, "No autofill fields found")
            callback.onSuccess(null)
            return
        }

        // Query database for matching entries
        val database = VaultDatabase.getDatabase(context)
        val encryption = Encryption()

        try {
            database.passwordEntryDao().getEntriesByUrl(packageName).let { entries ->
                if (entries.isEmpty()) {
                    Log.d(TAG, "No vault entries for package: $packageName")
                    callback.onSuccess(null)
                } else {
                    // Create autofill datasets for each entry
                    val fillResponse = AutofillResponseBuilder(context)
                        .addDatasets(entries, fields, encryption)
                        .build()
                    callback.onSuccess(fillResponse)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing autofill request", e)
            callback.onFailure("Error: ${e.message}")
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d(TAG, "Save request received")
        callback.onSuccess()
    }
}