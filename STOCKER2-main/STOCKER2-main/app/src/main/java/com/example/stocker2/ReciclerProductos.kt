package com.example.stocker2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocker2.adapter.ProductosAdapter
import com.example.stocker2.databinding.ActivityReciclerProductosBinding
import com.google.firebase.firestore.FirebaseFirestore

class ReciclerProductos : AppCompatActivity(), ProductosAdapter.OnItemClickListener {

    private lateinit var binding: ActivityReciclerProductosBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var btn_atras: ImageView
    private val myCollection = db.collection("Productos")
    private lateinit var adapter: ProductosAdapter
    val manager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()
        initRecyclerView()
        cargarDatosDesdeFirestore()
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
    }

    private fun crearObjetosDelXml() {
        binding = ActivityReciclerProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initRecyclerView() {
        binding.recyclerProductos.layoutManager = manager
        adapter = ProductosAdapter()
        binding.recyclerProductos.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    private fun cargarDatosDesdeFirestore() {
        myCollection.get().addOnSuccessListener { querySnapshot ->
            val productos = ArrayList<Productos>()
            for (document in querySnapshot) {
                val producto = document.toObject(Productos::class.java)
                productos.add(producto)
            }
            adapter.setProductos(productos)
        }.addOnFailureListener { e ->

        }
    }

    override fun onItemClick(productos: Productos) {
        val intent = Intent(this, VerProducto::class.java)
        intent.putExtra("NombreProducto", productos.nombre)
        startActivity(intent)
    }
}
