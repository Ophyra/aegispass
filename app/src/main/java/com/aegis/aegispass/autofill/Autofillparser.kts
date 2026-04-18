package com.aegis.aegispass.autofill

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId

data class AutofillField(
    val autofillId: AutofillId,
    val autofillType: Int,
    val autofillHints: Array<String>?,
    val hint: String?,
    val isFocused: Boolean
)

class AutofillParser(private val structure: AssistStructure) {
    fun parse(): List<AutofillField> {
        val fields = mutableListOf<AutofillField>()
        parse(structure.getWindowNodeAt(0).rootViewNode, fields)
        return fields
    }

    private fun parse(node: AssistStructure.ViewNode?, fields: MutableList<AutofillField>) {
        if (node == null) return

        if (isAutofillableField(node)) {
            val field = AutofillField(
                autofillId = node.autofillId ?: return,
                autofillType = node.autofillType,
                autofillHints = node.autofillHints,
                hint = node.hint,
                isFocused = node.isFocused
            )
            fields.add(field)
        }

        for (i in 0 until node.childCount) {
            parse(node.getChildAt(i), fields)
        }
    }

    private fun isAutofillableField(node: AssistStructure.ViewNode): Boolean {
        // Check if field has autofill hints or looks like username/password field
        val hints = node.autofillHints
        if (hints != null && hints.isNotEmpty()) {
            return hints.any { hint ->
                hint.contains("username", ignoreCase = true) ||
                        hint.contains("email", ignoreCase = true) ||
                        hint.contains("password", ignoreCase = true)
            }
        }

        // Fallback: check the hint text
        val hint = node.hint?.lowercase() ?: return false
        return hint.contains("username") || hint.contains("email") || hint.contains("password")
    }
}