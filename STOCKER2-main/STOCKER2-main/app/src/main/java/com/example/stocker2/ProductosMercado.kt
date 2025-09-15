package com.example.stocker2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stocker2.adapter.ProductosMercadoAdapter
import com.example.stocker2.databinding.ActivityProductosMercadoBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore

class ProductosMercado : AppCompatActivity() {

    private lateinit var binding: ActivityProductosMercadoBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var productosAdapter: ProductosMercadoAdapter
    private lateinit var productosList: ArrayList<Productos2>
    private var id: String? = null
    private var NombreEmpresa: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val objIntent: Intent = intent
        NombreEmpresa = objIntent.getStringExtra("NombreEmpresa")
        id = objIntent.getStringExtra("id")

        binding = ActivityProductosMercadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textViewNombMerc.text = NombreEmpresa
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.appbar.btnAtras.setOnClickListener {
            finish()
        }

        binding.btnOpenmap.setOnClickListener {
            id?.let { it1 ->
                db.collection("supermercados").document(it1).get().addOnSuccessListener { document ->
                    if (document.exists() && document.contains("latitudubi")) {
                        val intent = Intent(this, VerEnMapa::class.java)
                        intent.putExtra("id", id)
                        startActivity(intent)
                    } else {
                        ToastUtil.showCustomToast(this, "Este supermercado no ha registrado aún su ubicación")
                    }
                }.addOnFailureListener {
                    ToastUtil.showCustomToast(this, "Error al verificar la información del supermercado")
                }
            }
        }

        binding.btnEntVid.setOnClickListener {
            id?.let { comprobarUrlVideoYIniciarActividad(it) }
        }

        productosList = ArrayList()
        productosAdapter = ProductosMercadoAdapter()
        binding.recyclerMercados.layoutManager = LinearLayoutManager(this)
        binding.recyclerMercados.adapter = productosAdapter

        id?.let { cargarProductos(it) }
    }

    private fun comprobarUrlVideoYIniciarActividad(id: String) {
        db.collection("supermercados").document(id).get().addOnSuccessListener { documento ->
            if (documento.exists() && documento.contains("urlVideo")) {
                val intent = Intent(this, VerVideo::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            } else {
                ToastUtil.showCustomToast(this, "Lo siento, este supermercado no tiene video promocional")
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Error al obtener datos", it)
            ToastUtil.showCustomToast(this, "Error al acceder a la información del supermercado")
        }
    }

    private fun cargarProductos(idSuper: String) {
        db.collection("supermercados").document(idSuper).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val productosStock = document["productosStock"]
                if (productosStock is List<*>) {
                    val productosStockList = productosStock.filterIsInstance<Map<String, Any>>()
                    for (productoMap in productosStockList) {
                        val idProducto = productoMap["idProducto"] as? String ?: continue
                        val cantidadStock = (productoMap["cantidad"] as? Number)?.toInt() ?: 0

                        if (cantidadStock > 0) {
                            db.collection("Productos").document(idProducto).get().addOnSuccessListener { productoDoc ->
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_act1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val objIntent: Intent = intent
        val id = objIntent.getStringExtra("id")

        return when (item.itemId) {
            R.id.Web -> {
                if (id != null) {
                    db.collection("supermercados").document(id).get().addOnSuccessListener { snapshot ->
                        val pagWeb = snapshot.getString("paginaweb")
                        if (!pagWeb.isNullOrBlank()) {
                            abrirPagina(pagWeb)
                        } else {
                            ToastUtil.showCustomToast(this, "El supermercado no tiene página web")
                        }
                    }
                }
                true
            }
            R.id.Contactaremail -> {
                if (id != null) {
                    db.collection("supermercados").document(id).get().addOnSuccessListener { snapshot ->
                        val email = snapshot.getString("correo")
                        Log.d("FirestoreData", "correo: $email")
                        if (!email.isNullOrBlank()) {
                            mandarEmail(email)
                        } else {
                            ToastUtil.showCustomToast(this, "El supermercado no tiene correo electrónico")
                        }
                    }
                }
                true
            }
            R.id.AcDe -> {
                val intent = Intent(this, AcercaDeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun abrirPagina(pagWeb: String) {
        val url = if (pagWeb.startsWith("http://") || pagWeb.startsWith("https://")) {
            pagWeb
        } else {
            "http://$pagWeb"
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }


    private fun mandarEmail(email: String) {
        Log.d("FirestoreData", "el correo ha entrado en la funcion $email")
        if (email.isEmpty() || email.isBlank()) {
            ToastUtil.showCustomToast(this, "El supermercado no tiene una dirección de correo electrónico")
        } else {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$email")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Contacto a empresa")
            intent.putExtra(Intent.EXTRA_TEXT, "")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                ToastUtil.showCustomToast(this, "No se encontró una aplicación de correo electrónico")
            }
        }
    }
}
