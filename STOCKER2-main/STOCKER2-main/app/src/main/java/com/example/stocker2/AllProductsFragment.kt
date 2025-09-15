package com.example.stocker2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocker2.adapter.ProductosAdapter
import com.example.stocker2.databinding.FragmentAllProductsBinding
import com.google.firebase.firestore.FirebaseFirestore

class AllProductsFragment : Fragment(), ProductosAdapter.OnItemClickListener {

    private var _binding: FragmentAllProductsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductosAdapter
    private val myCollection = db.collection("Productos")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        cargarDatosDesdeFirestore()

    }

    private fun initRecyclerView() {
        binding.recyclerProductos.layoutManager = LinearLayoutManager(context)
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
        val intent = Intent(activity, VerProducto::class.java)
        intent.putExtra("NombreProducto", productos.nombre)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
