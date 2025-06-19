package com.example.breaktime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProductsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // Инициализация Firebase
        auth = Firebase.auth
        databaseManager = DatabaseManager.getInstance()

        // Проверка авторизации
        if (auth.currentUser == null) {
            redirectToMain()
            return
        }

        // Настройка тулбара
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(resources.getColor(R.color.black, null))
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle?.text = "Товары"

        // Настройка RecyclerView
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        productsRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Загружаем корзину и товары
        loadCartAndProducts()

        // Настройка нижнего меню
        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu -> {
                    // Уже на этом экране, ничего не делаем
                    true
                }
                R.id.cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
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

    }

    private fun loadCartAndProducts() {
        val userId = auth.currentUser?.uid ?: return
        databaseManager.getCart(userId) { cartItems ->
            val cartProductIds = cartItems.map { it.product.id }.toSet()
            loadProducts(cartProductIds)
        }
    }

    private fun loadProducts(cartProductIds: Set<String>) {
        databaseManager.getProducts { products ->
            val adapter = ProductsAdapter(products,
                { product -> addToCart(product) },
                { product -> removeFromCart(product) },
                cartProductIds)
            productsRecyclerView.adapter = adapter
        }
    }

    private fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            databaseManager.addToCart(userId, product)
            Toast.makeText(this, "${product.name} добавлен в корзину", Toast.LENGTH_SHORT).show()
            loadCartAndProducts()
        } else {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromCart(product: Product) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            databaseManager.removeFromCart(userId, product.id)
            Toast.makeText(this, "${product.name} удалён из корзины", Toast.LENGTH_SHORT).show()
            loadCartAndProducts()
        }
    }

    private fun redirectToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
} 