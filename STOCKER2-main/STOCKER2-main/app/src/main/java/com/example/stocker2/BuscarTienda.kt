package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocker2.adapter.SupermercadosAdapter
import com.example.stocker2.databinding.ActivityBuscarTiendaBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BuscarTienda : AppCompatActivity(), SupermercadosAdapter.OnItemClickListener {
    private lateinit var binding: ActivityBuscarTiendaBinding
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("supermercados")
    private lateinit var btn_atras: ImageView
    private lateinit var adapter: SupermercadosAdapter
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    val manager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialización de la vista y de los elementos
        crearObjetosDelXml()

        // Configuración del RecyclerView
        initRecicleView()

        // Configuración del lanzador de resultados de actividad para actualizar la vista después de ciertas acciones
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            initRecicleView()
        }

        // Retraso para cargar datos después de un segundo
        GlobalScope.launch {
            delay(1000)
            cargarDatosDesdeFirestore()
        }

        // Configuración de la barra de acción
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuración del botón de retroceso
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
    }

    /**
     * Método que crea objetos a partir del diseño XML.
     */
    private fun crearObjetosDelXml() {
        binding = ActivityBuscarTiendaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Método que crea el menú de opciones en la barra de acción.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main2, menu)
        return true
    }

    /**
     * Método que inicializa el RecyclerView y el adaptador.
     */
    private fun initRecicleView() {
        val decoration = DividerItemDecoration(this, manager.orientation)
        binding.recyclerMercados.layoutManager = manager
        adapter = SupermercadosAdapter()
        binding.recyclerMercados.adapter = adapter
        binding.recyclerMercados.addItemDecoration(decoration)
        adapter.setOnItemClickListener(this)

        // Obtener valores de preferencias para filtrar resultados
        val mySharedPreferences =
            getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        adapter.setFiltroCiudad(mySharedPreferences.getString("ciudad", ""))
        adapter.setFiltroSucursal(mySharedPreferences.getString("sucursal", ""))
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
                    // Nombre del documento en Firestore
                    var supermercado = document.toObject(SuperMercado::class.java)
                    // Asignar el nombre del supermercado
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
        val intent = Intent(this, ProductosMercado::class.java)
        intent.putExtra("NombreEmpresa", supermercado.nombre)
        intent.putExtra("id", supermercado.id.toString())
        startActivity(intent)
    }

    /**
     * Método llamado cuando se selecciona un elemento del menú de opciones.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.AcDe -> {
                val intent = Intent(this, AcercaDeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.Preferencias -> {
                val intent = Intent(this, PREFERENCIAS::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}




