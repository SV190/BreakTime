package com.example.breaktime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        val messageMetaTextView: TextView = view.findViewById(R.id.messageMetaTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.messageTextView.text = msg.message
        val date = Date(msg.timestamp)
        val sdf = SimpleDateFormat("HH:mm dd.MM.yy", Locale.getDefault())
        holder.messageMetaTextView.text = "${msg.userName}, ${sdf.format(date)}"
    }

    override fun getItemCount() = messages.size
} 