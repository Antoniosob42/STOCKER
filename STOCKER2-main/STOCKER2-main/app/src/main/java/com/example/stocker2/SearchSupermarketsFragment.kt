package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocker2.adapter.SupermercadosAdapter
import com.example.stocker2.databinding.FragmentSearchSupermarketsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SearchSupermarketsFragment : Fragment(), SupermercadosAdapter.OnItemClickListener {

    private var _binding: FragmentSearchSupermarketsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("supermercados")
    private lateinit var adapter: SupermercadosAdapter
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var manager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchSupermarketsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        initRecyclerView()

        // Configuración del lanzador de resultados de actividad para actualizar la vista después de ciertas acciones
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            initRecyclerView()
        }

        // Usar viewLifecycleOwner.lifecycleScope para manejar la coroutine correctamente
        viewLifecycleOwner.lifecycleScope.launch {
            cargarDatosDesdeFirestore()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar las preferencias y actualizar el RecyclerView
        val mySharedPreferences = requireContext().getSharedPreferences(requireContext().packageName + "_preferences", Context.MODE_PRIVATE)
        val ciudad = mySharedPreferences.getString("ciudad", "")
        val sucursal = mySharedPreferences.getString("sucursal", "")

        Log.d("SearchSupermarketsFragment", "Ciudad: $ciudad, Sucursal: $sucursal")

        adapter.setFiltroCiudad(ciudad)
        adapter.setFiltroSucursal(sucursal)

        cargarDatosDesdeFirestore()
    }

    /**
     * Método que inicializa el RecyclerView y el adaptador.
     */
    private fun initRecyclerView() {
        manager = LinearLayoutManager(requireContext())
        val decoration = DividerItemDecoration(requireContext(), manager.orientation)
        binding.recyclerMercados.layoutManager = manager
        adapter = SupermercadosAdapter()
        binding.recyclerMercados.adapter = adapter
        binding.recyclerMercados.addItemDecoration(decoration)
        adapter.setOnItemClickListener(this)
    }

    /**
     * Método que carga datos desde Firestore y actualiza el adaptador del RecyclerView.
     */
    private fun cargarDatosDesdeFirestore() {
        myCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val supermercados = ArrayList<SuperMercado>()
                for (document in querySnapshot) {
                    val supermercado = document.toObject(SuperMercado::class.java)
                    supermercados.add(supermercado)
                }
                Log.d("FirestoreData", "Supermercados: $supermercados")
                adapter.setSupermercados(supermercados)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreData", "Error al obtener datos de Firestore: $e")
            }
    }

    /**
     * Método llamado cuando se hace clic en un elemento del RecyclerView.
     */
    override fun onItemClick(supermercado: SuperMercado) {
        val intent = Intent(requireContext(), ProductosMercado::class.java)
        intent.putExtra("NombreEmpresa", supermercado.nombre)
        intent.putExtra("id", supermercado.id.toString())
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
