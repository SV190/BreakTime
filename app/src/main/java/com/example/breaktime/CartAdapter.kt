package com.example.breaktime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
        val decreaseButton: Button = view.findViewById(R.id.decreaseButton)
        val increaseButton: Button = view.findViewById(R.id.increaseButton)
        val removeButton: Button = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        
        holder.productName.text = cartItem.product.name
        holder.productPrice.text = "${cartItem.product.price} ₽"
        holder.quantityText.text = cartItem.quantity.toString()
        holder.totalPrice.text = "Итого: ${cartItem.getTotalPrice()} ₽"
        
        // Здесь можно добавить загрузку изображения через Glide или Picasso
        // holder.productImage.setImageResource(R.drawable.korzhik)
        
        holder.decreaseButton.setOnClickListener {
            val newQuantity = cartItem.quantity - 1
            onQuantityChanged(cartItem, newQuantity)
        }
        
        holder.increaseButton.setOnClickListener {
            val newQuantity = cartItem.quantity + 1
            onQuantityChanged(cartItem, newQuantity)
        }
        
        holder.removeButton.setOnClickListener {
            onRemoveItem(cartItem)
        }
    }

    override fun getItemCount() = cartItems.size

    fun updateCartItems(newCartItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }
} 