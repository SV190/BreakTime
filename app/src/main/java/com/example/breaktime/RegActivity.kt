package com.example.breaktime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegActivity : AppCompatActivity() {  // Изменено на RegActivity

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var LoginTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        //вью
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        LoginTv = findViewById(R.id.goToLoginActivity)

        val emailEt = findViewById<EditText>(R.id.email_et)
        val passwordEt = findViewById<EditText>(R.id.password_et)
        val usernameEt = findViewById<EditText>(R.id.username_et)
        val signUpBtn = findViewById<Button>(R.id.reg_btn)


            //кнопки
        signUpBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val username = usernameEt.text.toString().trim()


            // Полея
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                showToast("Fields cannot be empty")
                return@setOnClickListener
            }

            // Создание пользователя в Firebase
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Сохраняем имя в Firebase Auth (displayName)
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        // Переходим на товары сразу после регистрации
                        startActivity(Intent(this, ProductsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                        // Сохраняем профиль в фоне
                        saveUserData(email, username)
                    }
                } else {
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
        }
        LoginTv.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))}
    }

    private fun saveUserData(email: String, username: String) {
        val userId = auth.currentUser?.uid ?: run {
            showToast("User creation failed")
            return
        }

        val userProfile = UserProfile(
            userId = userId,
            name = username,
            email = email,
            phone = "",
            address = "",
            totalOrders = 0,
            completedOrders = 0,
            totalSpent = 0.0
        )
        // Сохраняем профиль в users (или Users, как в остальном приложении)
        database.reference.child("Users").child(userId).setValue(userProfile)
            .addOnSuccessListener {
                // После регистрации и сохранения профиля сразу переходим на товары
                startActivity(Intent(this, ProductsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .addOnFailureListener {
                showToast("Failed to save user data")
            }
    }

    //уведомления
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}