package com.example.stocker2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stocker2.adapter.ProductosMercadoAdapter
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productosAdapter: ProductosMercadoAdapter
    private lateinit var productosList: ArrayList<Productos2>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var idSuper: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idSuper = it.getString("idSuper")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerMercados)
        recyclerView.layoutManager = LinearLayoutManager(context)
        productosList = ArrayList()
        productosAdapter = ProductosMercadoAdapter()
        recyclerView.adapter = productosAdapter

        cargarProductos()

        setFragmentResultListener("productoActualizado") { _, _ ->
            cargarProductos()
        }
    }

    private fun cargarProductos() {
        db.collection("supermercados").document(idSuper).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val productosStock = document["productosStock"]
                    if (productosStock is List<*>) {
                        val productosStockList = productosStock.filterIsInstance<Map<String, Any>>()
                        productosList.clear()
                        for (productoMap in productosStockList) {
                            val idProducto = productoMap["idProducto"] as? String ?: continue
                            val cantidadStock = (productoMap["cantidad"] as? Number)?.toInt() ?: 0

                            db.collection("Productos").document(idProducto).get()
                                .addOnSuccessListener { productoDoc ->
                                    if (productoDoc.exists()) {
                                        val producto = productoDoc.toObject(Productos2::class.java)
                                        producto?.let {
                                            it.cantidadStock = cantidadStock
                                            productosList.add(it)
                                            productosAdapter.setProductos(productosList)
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(idSuper: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("idSuper", idSuper)
                }
            }
    }
}
