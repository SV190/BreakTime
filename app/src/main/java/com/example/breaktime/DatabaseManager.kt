package com.example.breaktime

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DatabaseManager {
    private val database: FirebaseDatabase = Firebase.database("https://breaktime-518fa-default-rtdb.europe-west1.firebasedatabase.app")
    private val productsRef = database.getReference("products")
    private val cartsRef = database.getReference("carts")

    init {
        // Проверяем и инициализируем товары при создании
        checkAndInitializeProducts()
        // Проверяем подключение к базе данных
        checkDatabaseConnection()
    }

    private fun checkDatabaseConnection() {
        productsRef.get().addOnSuccessListener { snapshot ->
            println("Подключение к Firebase Database успешно")
        }.addOnFailureListener { exception ->
            println("Ошибка подключения к Firebase Database: ${exception.message}")
        }
    }

    private fun checkAndInitializeProducts() {
        productsRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                println("База данных пуста, инициализируем товары...")
                initializeSampleProducts()
            } else {
                println("В базе данных найдено ${snapshot.childrenCount} товаров")
            }
        }
    }

    // Получение всех товаров
    fun getProducts(callback: (List<Product>) -> Unit) {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    try {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { products.add(it) }
                    } catch (e: Exception) {
                        println("Ошибка десериализации Product: ${e.message}")
                        // Попробуем десериализовать вручную
                        val product = Product(
                            id = productSnapshot.child("id").getValue(String::class.java) ?: productSnapshot.key ?: "",
                            name = productSnapshot.child("name").getValue(String::class.java) ?: "",
                            description = productSnapshot.child("description").getValue(String::class.java) ?: "",
                            price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0,
                            imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java) ?: "",
                            category = productSnapshot.child("category").getValue(String::class.java) ?: ""
                        )
                        products.add(product)
                    }
                }
                callback(products)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    // Добавление товара в корзину
    fun addToCart(userId: String, product: Product, quantity: Int = 1) {
        val cartItemRef = cartsRef.child(userId).child(product.id)
        cartItemRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Товар уже есть в корзине, увеличиваем количество
                val currentQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
                cartItemRef.child("quantity").setValue(currentQuantity + quantity)
            } else {
                // Добавляем новый товар в корзину
                val cartItem = CartItem(product, quantity)
                cartItemRef.setValue(cartItem)
            }
        }
    }

    // Получение корзины пользователя
    fun getCart(userId: String, callback: (List<CartItem>) -> Unit) {
        cartsRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItem>()
                for (itemSnapshot in snapshot.children) {
                    try {
                        val cartItem = itemSnapshot.getValue(CartItem::class.java)
                        cartItem?.let { cartItems.add(it) }
                    } catch (e: Exception) {
                        println("Ошибка десериализации CartItem: ${e.message}")
                        // Попробуем десериализовать вручную
                        val productId = itemSnapshot.key
                        val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 1
                        val productSnapshot = itemSnapshot.child("product")
                        
                        val product = Product(
                            id = productSnapshot.child("id").getValue(String::class.java) ?: productId ?: "",
                            name = productSnapshot.child("name").getValue(String::class.java) ?: "",
                            description = productSnapshot.child("description").getValue(String::class.java) ?: "",
                            price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0,
                            imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java) ?: "",
                            category = productSnapshot.child("category").getValue(String::class.java) ?: ""
                        )
                        
                        cartItems.add(CartItem(product, quantity))
                    }
                }
                callback(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    // Однократное получение корзины пользователя (без подписки)
    fun getCartOnce(userId: String, callback: (List<CartItem>) -> Unit) {
        cartsRef.child(userId).get().addOnSuccessListener { snapshot ->
            val cartItems = mutableListOf<CartItem>()
            for (itemSnapshot in snapshot.children) {
                try {
                    val cartItem = itemSnapshot.getValue(CartItem::class.java)
                    cartItem?.let { cartItems.add(it) }
                } catch (e: Exception) {
                    // Ручная десериализация, как в getCart
                    val productId = itemSnapshot.key
                    val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 1
                    val productSnapshot = itemSnapshot.child("product")
                    val product = Product(
                        id = productSnapshot.child("id").getValue(String::class.java) ?: productId ?: "",
                        name = productSnapshot.child("name").getValue(String::class.java) ?: "",
                        description = productSnapshot.child("description").getValue(String::class.java) ?: "",
                        price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0,
                        imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java) ?: "",
                        category = productSnapshot.child("category").getValue(String::class.java) ?: ""
                    )
                    cartItems.add(CartItem(product, quantity))
                }
            }
            callback(cartItems)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // Обновление количества товара в корзине
    fun updateCartItemQuantity(userId: String, productId: String, quantity: Int) {
        if (quantity <= 0) {
            // Удаляем товар из корзины
            cartsRef.child(userId).child(productId).removeValue()
        } else {
            // Обновляем количество
            cartsRef.child(userId).child(productId).child("quantity").setValue(quantity)
        }
    }

    // Удаление товара из корзины
    fun removeFromCart(userId: String, productId: String) {
        cartsRef.child(userId).child(productId).removeValue()
    }

    // Очистка корзины
    fun clearCart(userId: String, callback: (Boolean) -> Unit) {
        database.reference.child("carts").child(userId).removeValue()
            .addOnSuccessListener { _ ->
                callback(true)
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при очистке корзины: ${exception.message}")
                callback(false)
            }
    }

    // Инициализация тестовых товаров в базе данных
    fun initializeSampleProducts() {
        println("Инициализация товаров в базе данных...")
        println("URL базы данных: ${database.reference}")
        val sampleProducts = listOf(
            Product(
                id = "1",
                name = "Коржик с кремом",
                description = "Свежий коржик с нежным кремом. Идеально для перекуса.",
                price = 299.0,
                imageUrl = "",
                category = "Выпечка"
            ),
            Product(
                id = "2",
                name = "Кофе Американо",
                description = "Классический кофе американо. Крепкий и ароматный.",
                price = 199.0,
                imageUrl = "",
                category = "Напитки"
            ),
            Product(
                id = "3",
                name = "Сэндвич с курицей",
                description = "Свежий сэндвич с куриным филе и овощами.",
                price = 399.0,
                imageUrl = "",
                category = "Сэндвичи"
            ),
            Product(
                id = "4",
                name = "Чай зеленый",
                description = "Освежающий зеленый чай с лимоном.",
                price = 149.0,
                imageUrl = "",
                category = "Напитки"
            ),
            Product(
                id = "5",
                name = "Пицца Маргарита",
                description = "Классическая пицца с томатами и моцареллой.",
                price = 599.0,
                imageUrl = "",
                category = "Пицца"
            ),
            Product(
                id = "6",
                name = "Салат Цезарь",
                description = "Свежий салат с курицей, сыром и соусом цезарь.",
                price = 449.0,
                imageUrl = "",
                category = "Салаты"
            )
        )

        var successCount = 0
        var failureCount = 0
        val totalProducts = sampleProducts.size

        // Добавляем товары в базу данных
        for (product in sampleProducts) {
            productsRef.child(product.id).setValue(product)
                .addOnSuccessListener {
                    println("Товар ${product.name} добавлен в базу данных")
                    successCount++
                    if (successCount + failureCount == totalProducts) {
                        println("Инициализация завершена: $successCount успешно, $failureCount ошибок")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Ошибка добавления товара ${product.name}: ${exception.message}")
                    failureCount++
                    if (successCount + failureCount == totalProducts) {
                        println("Инициализация завершена: $successCount успешно, $failureCount ошибок")
                    }
                }
        }
    }

    // Альтернативный метод инициализации через транзакцию
    fun initializeSampleProductsAlternative() {
        println("Альтернативная инициализация товаров...")
        val sampleProducts = listOf(
            Product("1", "Коржик с кремом", "Свежий коржик с нежным кремом", 299.0, "", "Выпечка"),
            Product("2", "Кофе Американо", "Классический кофе американо", 199.0, "", "Напитки"),
            Product("3", "Сэндвич с курицей", "Свежий сэндвич с куриным филе", 399.0, "", "Сэндвичи"),
            Product("4", "Чай зеленый", "Освежающий зеленый чай", 149.0, "", "Напитки")
        )
        
        val updates = mutableMapOf<String, Any>()
        for (product in sampleProducts) {
            updates["products/${product.id}"] = product
        }
        
        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                println("Альтернативная инициализация успешно завершена")
            }
            .addOnFailureListener { exception ->
                println("Ошибка альтернативной инициализации: ${exception.message}")
            }
    }

    // Методы для работы с заказами
    fun createOrder(order: Order, callback: (Boolean) -> Unit) {
        val orderId = database.reference.child("orders").push().key ?: return
        val orderWithId = order.copy(id = orderId)
        
        database.reference.child("orders").child(orderId).setValue(orderWithId)
            .addOnSuccessListener { _ ->
                // Обновляем статистику пользователя
                updateUserStats(order.userId, order.totalAmount)
                callback(true)
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при создании заказа: ${exception.message}")
                callback(false)
            }
    }

    fun getOrders(userId: String, callback: (List<Order>) -> Unit) {
        database.reference.child("orders").orderByChild("userId").equalTo(userId)
            .get()
            .addOnSuccessListener { snapshot: DataSnapshot ->
                val orders = mutableListOf<Order>()
                for (child in snapshot.children) {
                    try {
                        val order = child.getValue(Order::class.java)
                        if (order != null) {
                            orders.add(order)
                        }
                    } catch (e: Exception) {
                        println("Ошибка при десериализации заказа: ${e.message}")
                    }
                }
                callback(orders.sortedByDescending { it.orderDate })
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при получении заказов: ${exception.message}")
                callback(emptyList())
            }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus, callback: (Boolean) -> Unit) {
        database.reference.child("orders").child(orderId).child("status").setValue(status.name)
            .addOnSuccessListener { _ ->
                if (status == OrderStatus.COMPLETED) {
                    // Получаем userId заказа
                    database.reference.child("orders").child(orderId).get().addOnSuccessListener { snapshot ->
                        val userId = snapshot.child("userId").getValue(String::class.java)
                        if (!userId.isNullOrEmpty()) {
                            // Увеличиваем completedOrders
                            database.reference.child("users").child(userId).get().addOnSuccessListener { userSnap ->
                                val profile = userSnap.getValue(UserProfile::class.java)
                                if (profile != null) {
                                    val updatedProfile = profile.copy(completedOrders = profile.completedOrders + 1)
                                    updateUserProfile(updatedProfile) { }
                                }
                            }
                        }
                    }
                }
                callback(true)
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при обновлении статуса заказа: ${exception.message}")
                callback(false)
            }
    }

    // Методы для работы с профилями пользователей
    fun getUserProfile(userId: String, callback: (UserProfile?) -> Unit) {
        database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot: DataSnapshot ->
                if (snapshot.exists()) {
                    try {
                        val profile = snapshot.getValue(UserProfile::class.java)
                        callback(profile)
                    } catch (e: Exception) {
                        println("Ошибка при десериализации профиля: ${e.message}")
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при получении профиля: ${exception.message}")
                callback(null)
            }
    }

    fun updateUserProfile(profile: UserProfile, callback: (Boolean) -> Unit) {
        database.reference.child("users").child(profile.userId).setValue(profile)
            .addOnSuccessListener { _ ->
                callback(true)
            }
            .addOnFailureListener { exception: Exception ->
                println("Ошибка при обновлении профиля: ${exception.message}")
                callback(false)
            }
    }

    private fun updateUserStats(userId: String, orderAmount: Double) {
        database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot: DataSnapshot ->
                val currentProfile = if (snapshot.exists()) {
                    try {
                        snapshot.getValue(UserProfile::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } else null

                val updatedProfile = currentProfile?.copy(
                    totalOrders = currentProfile.totalOrders + 1,
                    totalSpent = currentProfile.totalSpent + orderAmount
                ) ?: UserProfile(
                    userId = userId,
                    totalOrders = 1,
                    totalSpent = orderAmount
                )

                updateUserProfile(updatedProfile) { success ->
                    if (success) {
                        println("Статистика пользователя обновлена")
                    }
                }
            }
    }

    // Отправка сообщения в чат (индивидуальный чат)
    fun sendChatMessage(userId: String, userName: String, message: String) {
        val chatRef = database.reference.child("chats").child(userId).child("messages")
        val chatMessage = ChatMessage(userId, userName, message, System.currentTimeMillis())
        chatRef.push().setValue(chatMessage)
    }

    // Получение всех сообщений чата (индивидуальный чат)
    fun getChatMessages(userId: String, callback: (List<ChatMessage>) -> Unit) {
        val chatRef = database.reference.child("chats").child(userId).child("messages")
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (msgSnapshot in snapshot.children) {
                    val msg = msgSnapshot.getValue(ChatMessage::class.java)
                    if (msg != null) messages.add(msg)
                }
                callback(messages.sortedBy { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseManager? = null

        fun getInstance(): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseManager()
                INSTANCE = instance
                instance
            }
        }
    }
} 