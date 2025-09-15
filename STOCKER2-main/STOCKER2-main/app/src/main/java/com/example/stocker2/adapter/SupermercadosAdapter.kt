package com.example.stocker2.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stocker2.SuperMercado
import com.example.stocker2.databinding.ItemSupermercadoBinding

import java.util.Locale

// Adaptador para la lista de supermercados en un RecyclerView
class SupermercadosAdapter : RecyclerView.Adapter<SupermercadosAdapter.SuperMercadoViewHolder>() {


    private var supermercados: MutableList<SuperMercado> = mutableListOf()
    private var filteredSupermercados: MutableList<SuperMercado> = mutableListOf()
    private var onItemClickListener: OnItemClickListener? = null
    private var filtroCiudad: String = ""
    private var filtroSucursal: String = ""

    // Crea y devuelve un nuevo ViewHolder para representar un ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuperMercadoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSupermercadoBinding.inflate(layoutInflater, parent, false)
        return SuperMercadoViewHolder(binding)
    }

    // Vincula los datos del ítem en la posición especificada en el ViewHolder
    override fun onBindViewHolder(holder: SuperMercadoViewHolder, position: Int) {
        val SuperMercado = filteredSupermercados[position]
        holder.render(SuperMercado)
    }

    // Devuelve la cantidad de elementos en la lista
    override fun getItemCount(): Int {
        return filteredSupermercados.size
    }

    // ViewHolder que representa cada ítem en la lista
    inner class SuperMercadoViewHolder(private val binding: ItemSupermercadoBinding):
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Inicializa el click listener para cada ítem
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(supermercados[position])
                }
            }
        }

        // Renderiza los datos del Supermercado en el ViewHolder
        fun render(Super: SuperMercado) {
            binding.textViewNombre.text = Super.nombre
            binding.textViewCE.text = Super.direccion
            binding.textViewCiudad.text = Super.Ciudad
            // Utiliza la biblioteca Glide para cargar una imagen desde una URL en el ImageView
            if(Super.urlImagen==""){
                Glide.with(binding.imageViewProducto.context)
                    .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS5GuV8OQCtWOlFIAOGTqpeKXuGUwFYHin5yA&usqp=CAU")
                    .into(binding.imageViewProducto)
            }
            else{
                Glide.with(binding.imageViewProducto.context)
                    .load(Super.urlImagen)
                    .into(binding.imageViewProducto)
            }




        }
    }

    // Establece la lista de supermercados y aplica los filtros
    fun setSupermercados(supermercados: MutableList<SuperMercado>) {
        this.supermercados = supermercados
        applyFiltros()
    }

    // Establece el filtro de ciudad y aplica los filtros
    fun setFiltroCiudad(ciudad: String?) {
        if (ciudad != null) {
            this.filtroCiudad = ciudad.lowercase(Locale.ROOT)
        }
        applyFiltros()
    }

    // Establece el filtro de sucursal y aplica los filtros
    fun setFiltroSucursal(Sucursal: String?) {
        if (Sucursal != null) {
            this.filtroSucursal = Sucursal.lowercase(Locale.ROOT)
        }
        applyFiltros()
    }

    // Aplica los filtros a la lista de supermercados
    @SuppressLint("NotifyDataSetChanged")
    private fun applyFiltros() {
        filteredSupermercados.clear()

        for (supermercado in supermercados) {
            val superciudad = supermercado.Ciudad.lowercase(Locale.ROOT)
            val superNombre = supermercado.nombre.lowercase(Locale.ROOT)

            // Filtra por ciudad y sucursal
            if ((filtroCiudad.isEmpty() || superciudad == filtroCiudad) &&
                (filtroSucursal.isEmpty() || superNombre == filtroSucursal)
            ) {
                Log.d("controlfiltro", " El filtro es este: filtroCiudad: $filtroCiudad y filtroSucursal: $filtroSucursal")
                Log.d("controlfiltro", " Superañadido:$supermercado")
                filteredSupermercados.add(supermercado)
            }
        }

        notifyDataSetChanged()
    }

    // Interfaz para manejar clics en los elementos del RecyclerView
    interface OnItemClickListener {
        fun onItemClick(supermercado: SuperMercado)
    }

    // Establece el listener para los clics en los elementos del RecyclerView
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}