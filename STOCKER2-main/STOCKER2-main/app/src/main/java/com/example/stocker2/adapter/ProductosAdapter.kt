package com.example.stocker2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stocker2.Productos
import com.example.stocker2.R
import com.example.stocker2.databinding.ItemProducto2Binding
/*Esta clase es la que se encarga de cargar todos los items productos que hay, tanto cuando se filtra de alguna manera
como cuando estan integros

 */
class ProductosAdapter : RecyclerView.Adapter<ProductosAdapter.ProductosViewHolder>() {
                    //la lista de los productos a introducir
    private var productos: MutableList<Productos> = mutableListOf()
            //la lista filtrada(si es necesario)
    private var filteredProductos: MutableList<Productos> = mutableListOf()
    //la funcion que manejará que un item se pulse dentro de el recyclerView
    private var onItemClickListener: OnItemClickListener? = null
            //la funcion de el ViewHolder que carga la estructura del ItemProducto2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemProducto2Binding.inflate(layoutInflater, parent, false)
        return ProductosViewHolder(binding)
    }
            //la funcion que renderiza los items dentro de el recyclerView
    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val producto = filteredProductos[position]
        holder.render(producto)
    }
            //funcion que devuelve el numero de productos
    override fun getItemCount(): Int {
        return filteredProductos.size
    }
            //El viewHolder, la clase que  gestiona el interior de los items del recycler
    inner class ProductosViewHolder(private val binding: ItemProducto2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(productos[position])
                }
            }
        }
            //Esta funcion asigna a cada item el texto correspondiente del producto correspondiente
        fun render(producto: Productos) {
            binding.textViewNombre.text = itemView.context.getString(R.string.nombre_producto, producto.nombre)
            binding.textViewCalorias.text = itemView.context.getString(R.string.calorias_text, producto.calorias.toString())
            binding.textViewGramos.text = itemView.context.getString(R.string.gramos_text, producto.gramos.toString())
            binding.textViewIndiceSano.text = itemView.context.getString(R.string.indicesan_text, producto.indiceSano.toString())
                //asigna la imagen si tiene, si no le pone la default
            if (producto.urlImagen.isEmpty()) {
                Glide.with(binding.imageViewProducto.context)
                    .load("https://www.example.com/default_image.jpg")
                    .into(binding.imageViewProducto)
            } else {
                Glide.with(binding.imageViewProducto.context)
                    .load(producto.urlImagen)
                    .into(binding.imageViewProducto)
            }
        }
    }
        //esta funcion filtra segun los patrones antes establecidos
    fun setProductos(productos: MutableList<Productos>) {
        val oldSize = filteredProductos.size
        this.productos = productos
        applyFiltros()
        val newSize = filteredProductos.size
        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (newSize < oldSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        } else {
            notifyItemRangeChanged(0, newSize)
        }
    }
        //esta funcion aplica los Filtros
    private fun applyFiltros() {
        filteredProductos.clear()
        filteredProductos.addAll(productos)
    }

    interface OnItemClickListener {
        fun onItemClick(productos: Productos)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}
