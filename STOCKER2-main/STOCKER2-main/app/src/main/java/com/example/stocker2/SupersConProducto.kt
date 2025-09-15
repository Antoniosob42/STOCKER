// SupersConProducto.kt
package com.example.stocker2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stocker2.adapter.SupermercadosAdapter
import com.example.stocker2.databinding.ActivitySupersConProductoBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore

class SupersConProducto : AppCompatActivity() {
    private lateinit var binding: ActivitySupersConProductoBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SupermercadosAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var productoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupersConProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        productoId = intent.getStringExtra("productoId") ?: ""

        recyclerView = binding.recyclerMercados
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SupermercadosAdapter()
        recyclerView.adapter = adapter

        cargarSupermercadosConProducto()

        binding.appbar.btnAtras.setOnClickListener {
            finish()
        }
    }

    private fun cargarSupermercadosConProducto() {
        db.collection("supermercados").get()
            .addOnSuccessListener { querySnapshot ->
                val supermercados = ArrayList<SuperMercado>()
                for (document in querySnapshot) {
                    val productosStock = document["productosStock"] as? List<*>
                    val productosStockList = productosStock?.filterIsInstance<Map<String, Any>>() ?: continue

                    for (productoMap in productosStockList) {
                        val idProducto = productoMap["idProducto"] as? String ?: continue
                        val cantidad = (productoMap["cantidad"] as? Number)?.toInt() ?: 0
                        if (idProducto == productoId && cantidad > 0) {
                            val supermercado = document.toObject(SuperMercado::class.java)
                            supermercados.add(supermercado)
                        }
                    }
                }
                if (supermercados.isEmpty()) {
                    ToastUtil.showCustomToast(this, "Este producto no se ha encontrado en ningún supermercado")
                    finish()  // Vuelve a la actividad anterior
                } else {
                    adapter.setSupermercados(supermercados)
                }
            }
            .addOnFailureListener { e ->
                ToastUtil.showCustomToast(this, "Error al cargar los datos")
                finish()  // Vuelve a la actividad anterior en caso de error
            }
    }
}
