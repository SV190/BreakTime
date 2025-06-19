package com.example.breaktime

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductsAdapter(
    private val products: List<Product>,
    private val onAddToCart: (Product) -> Unit,
    private val onRemoveFromCart: (Product) -> Unit,
    private val cartProductIds: Set<String>
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val addToCartButton: Button = view.findViewById(R.id.addToCartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        
        holder.productName.text = product.name
        holder.productPrice.text = "${product.price} ₽"
        
        // Загрузка изображения по URL через Glide
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.productImage.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.korzhik)
                .error(R.drawable.korzhik)
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(R.drawable.korzhik)
        }
        
        val context = holder.addToCartButton.context
        val inCart = cartProductIds.contains(product.id)
        if (inCart) {
            holder.addToCartButton.setBackgroundResource(R.drawable.round_button_bg)
            holder.addToCartButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
            holder.addToCartButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_delete, 0, 0)
            holder.addToCartButton.setOnClickListener {
                onRemoveFromCart(product)
            }
        } else {
            holder.addToCartButton.setBackgroundResource(R.drawable.round_button_bg)
            holder.addToCartButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            holder.addToCartButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_cart, 0, 0)
            holder.addToCartButton.setOnClickListener {
                onAddToCart(product)
            }
        }
    }

    override fun getItemCount() = products.size
} 