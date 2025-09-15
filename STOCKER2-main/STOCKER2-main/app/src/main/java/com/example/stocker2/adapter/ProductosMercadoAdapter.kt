package com.example.stocker2.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stocker2.Productos2
import com.example.stocker2.R
import com.example.stocker2.databinding.ItemProductoBinding
//Es una clase copia de el anterior ProductosAdapter pero única para los recyclerviews que muestran
//productos dentro de un supermercado específico.
class ProductosMercadoAdapter : RecyclerView.Adapter<ProductosMercadoAdapter.ProductosViewHolder>() {

    private var productos: MutableList<Productos2> = mutableListOf()
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemProductoBinding.inflate(layoutInflater, parent, false)
        return ProductosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto)
    }

    override fun getItemCount(): Int = productos.size

    inner class ProductosViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(productos[position])
                }
            }
        }

        fun bind(producto: Productos2) {
            binding.apply {
                textViewNombre.text = itemView.context.getString(R.string.nombre_producto, producto.nombre)
                textViewCalorias.text = itemView.context.getString(R.string.calorias_text, producto.calorias.toString())
                textViewGramos.text = itemView.context.getString(R.string.gramos_text, producto.gramos.toString())
                textViewIndiceSano.text = itemView.context.getString(R.string.indicesan_text, producto.indiceSano.toString())
                textViewCantStock.text = itemView.context.getString(R.string.cantidad_stock_text, producto.cantidadStock.toString())
                textViewCantStock.isVisible = true

                // Set text color to black
                val blackColor = Color.BLACK
                textViewNombre.setTextColor(blackColor)
                textViewCalorias.setTextColor(blackColor)
                textViewGramos.setTextColor(blackColor)
                textViewIndiceSano.setTextColor(blackColor)
                textViewCantStock.setTextColor(blackColor)

                if (producto.urlImagen.isEmpty()) {
                    Glide.with(imageViewProducto.context)
                        .load("https://www.example.com/default_image.png")
                        .into(imageViewProducto)
                } else {
                    Glide.with(imageViewProducto.context)
                        .load(producto.urlImagen)
                        .into(imageViewProducto)
                }

                when {
                    producto.cantidadStock == 0 -> root.setBackgroundColor(Color.rgb(255, 201, 201)) // Rojo
                    producto.cantidadStock < 10 -> root.setBackgroundColor(Color.rgb(107, 174, 24)) // Amarillo
                    else -> root.setBackgroundColor(Color.rgb(54, 223, 144))  // Verde
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setProductos(nuevosProductos: MutableList<Productos2>) {
        productos = nuevosProductos
        notifyDataSetChanged() // Utilizamos notifyDataSetChanged como se hacía originalmente
    }

    interface OnItemClickListener {
        fun onItemClick(productos: Productos2)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}
