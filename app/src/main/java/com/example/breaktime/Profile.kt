package com.example.breaktime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat

class Profile : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutBtn: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var ordersCompletedText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var editProfileButton: Button
    private lateinit var orderHistoryButton: Button
    private lateinit var databaseManager: DatabaseManager
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Настройка edge-to-edge
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        // val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // windowInsetsController.apply {
        //     hide(WindowInsetsCompat.Type.systemBars())
        //     systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // }

        // Инициализация Firebase
        auth = Firebase.auth
        databaseManager = DatabaseManager.getInstance()

        // Проверка авторизации
        val user = auth.currentUser
        if (user == null) {
            redirectToMain()
            return
        }
        currentUserId = user.uid

        // Настройка тулбара
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(resources.getColor(R.color.black, null))
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle?.text = "Профиль"

        // Инициализация элементов интерфейса
        userNameText = findViewById(R.id.userNameText)
        userEmailText = findViewById(R.id.userEmailText)
        ordersCompletedText = findViewById(R.id.ordersCompletedText)
        totalSpentText = findViewById(R.id.totalSpentText)
        editProfileButton = findViewById(R.id.editProfileButton)
        orderHistoryButton = findViewById(R.id.orderHistoryButton)
        
        // Загрузка данных пользователя
        loadUserProfile()
        
        // Обработка кнопок
        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
        
        orderHistoryButton.setOnClickListener {
            showOrderHistory()
        }

        // Настройка нижнего меню
        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.profile

        bottomNav.setOnItemSelectedListener { item ->
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
                    startActivity(Intent(this, ChatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }

        // Корректно учитываем статус-бар
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(0, systemBars.top, 0, 0)
        //     insets
        // }

        // Кнопка выхода (теперь только внизу)
        logoutBtn = findViewById(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        val userId = user?.uid ?: return
        databaseManager.getUserProfile(userId) { profile ->
            runOnUiThread {
                if (profile != null) {
                    // Загружаем заказы пользователя
                    databaseManager.getOrders(userId) { orders ->
                        val completedCount = orders.count { it.status == OrderStatus.COMPLETED }
                        val updatedProfile = profile.copy(
                            name = user.displayName.orEmpty().ifEmpty { profile.name },
                            email = user.email.orEmpty().ifEmpty { profile.email },
                            completedOrders = completedCount
                        )
                        updateProfileUI(updatedProfile)
                    }
                } else {
                    // Если профиля нет, создаем его на основе данных из Firebase Auth
                    val name = user.displayName.orEmpty().ifEmpty { "Пользователь" }
                    val email = user.email.orEmpty().ifEmpty { "user@example.com" }
                    val defaultProfile = UserProfile(
                        userId = userId,
                        name = name,
                        email = email,
                        phone = "",
                        address = ""
                    )
                    databaseManager.updateUserProfile(defaultProfile) { success ->
                        if (success) {
                            runOnUiThread {
                                updateProfileUI(defaultProfile)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateProfileUI(profile: UserProfile) {
        userNameText.text = profile.name
        userEmailText.text = profile.email
        ordersCompletedText.text = profile.completedOrders.toString()
        totalSpentText.text = "${profile.totalSpent} ₽"
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val userId = auth.currentUser?.uid ?: return
        // Загружаем текущие данные
        databaseManager.getUserProfile(userId) { profile ->
            if (profile != null) {
                runOnUiThread {
                    dialogView.findViewById<TextInputEditText>(R.id.nameInput).setText(profile.name)
                    dialogView.findViewById<TextInputEditText>(R.id.emailInput).setText(profile.email)
                    dialogView.findViewById<TextInputEditText>(R.id.phoneInput).setText(profile.phone)
                    dialogView.findViewById<TextInputEditText>(R.id.addressInput).setText(profile.address)
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Редактировать профиль")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.nameInput).text.toString()
                val email = dialogView.findViewById<TextInputEditText>(R.id.emailInput).text.toString()
                val phone = dialogView.findViewById<TextInputEditText>(R.id.phoneInput).text.toString()
                val address = dialogView.findViewById<TextInputEditText>(R.id.addressInput).text.toString()

                val updatedProfile = UserProfile(
                    userId = userId,
                    name = name,
                    email = email,
                    phone = phone,
                    address = address
                )

                databaseManager.updateUserProfile(updatedProfile) { success ->
                    if (success) {
                        runOnUiThread {
                            updateProfileUI(updatedProfile)
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun showOrderHistory() {
        val userId = auth.currentUser?.uid ?: return
        databaseManager.getOrders(userId) { orders ->
            runOnUiThread {
                if (orders.isNotEmpty()) {
                    showOrdersDialog(orders)
                } else {
                    showEmptyOrdersDialog()
                }
            }
        }
    }

    private fun showOrdersDialog(orders: List<Order>) {
        val orderList = orders.joinToString("\n\n") { order ->
            "Заказ #${order.id.take(8)}\n" +
            "Дата: ${order.orderDate}\n" +
            "Статус: ${getStatusText(order.status)}\n" +
            "Сумма: ₽${order.totalAmount}\n" +
            "Товары: ${order.items.size} шт."
        }

        AlertDialog.Builder(this)
            .setTitle("История заказов")
            .setMessage(orderList)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun showEmptyOrdersDialog() {
        AlertDialog.Builder(this)
            .setTitle("История заказов")
            .setMessage("У вас пока нет заказов")
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun getStatusText(status: OrderStatus): String {
        return when (status) {
            OrderStatus.PENDING -> "Ожидает подтверждения"
            OrderStatus.CONFIRMED -> "Подтвержден"
            OrderStatus.PREPARING -> "Готовится"
            OrderStatus.DELIVERING -> "Доставляется"
            OrderStatus.COMPLETED -> "Выполнен"
            OrderStatus.CANCELLED -> "Отменен"
        }
    }

    private fun logout() {
        auth.signOut()
        redirectToMain()
    }

    private fun redirectToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}