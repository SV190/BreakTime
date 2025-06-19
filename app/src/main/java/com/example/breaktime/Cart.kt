package com.example.breaktime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import java.util.*

class Cart : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var checkoutButton: Button
    private lateinit var databaseManager: DatabaseManager
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


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
        toolbarTitle?.text = "Корзина"

        // Настройка RecyclerView для корзины
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Настройка элементов интерфейса
        totalPriceText = findViewById(R.id.totalPrice)
        checkoutButton = findViewById(R.id.checkoutButton)
        
        // Загрузка корзины
        loadCart()
        
        // Обработка кнопки оформления заказа
        checkoutButton.setOnClickListener {
            checkout()
        }

        // Настройка нижнего меню
        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.cart

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu -> {
                    startActivity(Intent(this, ProductsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.cart -> {
                    // Уже на этом экране, ничего не делаем
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

    private fun loadCart() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            databaseManager.getCart(userId) { cartItems ->
                cartAdapter = CartAdapter(
                    cartItems.toMutableList(),
                    onQuantityChanged = { cartItem, newQuantity ->
                        updateCartItemQuantity(userId, cartItem, newQuantity)
                    },
                    onRemoveItem = { cartItem ->
                        removeFromCart(userId, cartItem)
                    }
                )
                cartRecyclerView.adapter = cartAdapter
                
                // Обновляем общую сумму
                updateTotalPrice(cartItems)
            }
        }
    }
    
    private fun updateCartItemQuantity(userId: String, cartItem: CartItem, newQuantity: Int) {
        databaseManager.updateCartItemQuantity(userId, cartItem.product.id, newQuantity)
    }
    
    private fun removeFromCart(userId: String, cartItem: CartItem) {
        databaseManager.removeFromCart(userId, cartItem.product.id)
        Toast.makeText(this, "${cartItem.product.name} удален из корзины", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateTotalPrice(cartItems: List<CartItem>) {
        val total = cartItems.sumOf { it.getTotalPrice() }
        totalPriceText.text = "₽ $total"
    }
    
    private fun checkout() {
        createOrder()
    }

    private fun redirectToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun createOrder() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            databaseManager.getUserProfile(userId) { profile ->
                val userName = profile?.name ?: "Пользователь"
                val address = profile?.address ?: "-"
                val phone = profile?.phone ?: "-"
                databaseManager.getCartOnce(userId) { cartItems ->
                    if (cartItems.isEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show()
                        }
                        return@getCartOnce
                    }

                    val totalAmount = cartItems.sumOf { it.getTotalPrice() }
                    val order = Order(
                        userId = userId,
                        items = cartItems,
                        totalAmount = totalAmount,
                        status = OrderStatus.COMPLETED,
                        orderDate = Date(),
                        deliveryAddress = address,
                        phoneNumber = phone
                    )

                    databaseManager.createOrder(order) { success ->
                        runOnUiThread {
                            if (success) {
                                databaseManager.clearCart(userId) { clearSuccess ->
                                    runOnUiThread {
                                        if (clearSuccess) {
                                            val emptyCartItems = mutableListOf<CartItem>()
                                            cartAdapter.updateCartItems(emptyCartItems)
                                            updateTotalPrice(emptyCartItems)
                                            Toast.makeText(this, "Заказ успешно создан!", Toast.LENGTH_LONG).show()
                                            // Профиль обновится при следующем открытии экрана профиля
                                            // Отправляем информацию о заказе в чат
                                            val itemsInfo = cartItems.joinToString("\n") { "${it.product.name} — ${it.quantity} шт." }
                                            val orderInfo = "\uD83D\uDED2 Новый заказ!\n" +
                                                    "Имя: $userName\n" +
                                                    "Адрес: $address\n" +
                                                    "Телефон: $phone\n" +
                                                    "Позиции:\n$itemsInfo\n" +
                                                    "Сумма: ₽$totalAmount\n" +
                                                    "Время: ${Date()}"
                                            databaseManager.sendChatMessage(userId, userName, orderInfo)
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Ошибка при создании заказа", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}