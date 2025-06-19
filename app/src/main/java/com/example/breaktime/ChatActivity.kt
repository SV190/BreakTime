package com.example.breaktime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.chat
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu -> {
                    startActivity(Intent(this, ProductsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.chat -> {
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }

        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendMessageButton = findViewById<Button>(R.id.sendMessageButton)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: ""
        val userName = user?.displayName ?: user?.email ?: "Пользователь"

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val toolbarTitle = android.widget.TextView(this)
        toolbarTitle.text = "Чат"
        toolbarTitle.textSize = 20f
        toolbarTitle.setTextColor(resources.getColor(R.color.black, null))
        toolbarTitle.setPadding(70, 0, 0, 0)
        toolbar.addView(toolbarTitle)

        // Загрузка сообщений
        DatabaseManager.getInstance().getChatMessages(userId) { messages ->
            runOnUiThread {
                chatRecyclerView.adapter = ChatAdapter(messages)
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // Отправка сообщения
        sendMessageButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                DatabaseManager.getInstance().sendChatMessage(userId, userName, text)
                messageEditText.text.clear()
            }
        }
    }
} 