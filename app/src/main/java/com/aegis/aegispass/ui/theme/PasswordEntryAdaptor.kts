package com.aegis.aegispass.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aegis.aegispass.R
import com.aegis.aegispass.database.PasswordEntry

class PasswordEntryAdapter(
    private val onItemClick: (PasswordEntry) -> Unit
) : ListAdapter<PasswordEntry, PasswordEntryAdapter.PasswordEntryViewHolder>(PasswordEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordEntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_password_entry, parent, false)
        return PasswordEntryViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: PasswordEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PasswordEntryViewHolder(
        itemView: View,
        private val onItemClick: (PasswordEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.entryTitle)
        private val usernameTextView: TextView = itemView.findViewById(R.id.entryUsername)
        private val urlTextView: TextView = itemView.findViewById(R.id.entryUrl)

        fun bind(entry: PasswordEntry) {
            titleTextView.text = entry.title
            usernameTextView.text = entry.username
            urlTextView.text = entry.url ?: "No URL"

            itemView.setOnClickListener {
                onItemClick(entry)
            }
        }
    }

    private class PasswordEntryDiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
        override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
            return oldItem == newItem
        }
    }
}
