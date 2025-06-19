package com.example.breaktime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация Firebase Auth
        auth = Firebase.auth

        // Проверка авторизации при запуске
        checkAuthAndRedirect()

        // кнопки
        val regBtn = findViewById<Button>(R.id.reg_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)

        // Обработка кнопки регистрации
        regBtn.setOnClickListener {
            startActivity(Intent(this, RegActivity::class.java))
        }

        // Обработка кнопки входа
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Повторная проверка при возвращении на экран
        checkAuthAndRedirect()
    }

    private fun checkAuthAndRedirect() {
        // Если пользователь авторизован, переход в ProductsActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, ProductsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}