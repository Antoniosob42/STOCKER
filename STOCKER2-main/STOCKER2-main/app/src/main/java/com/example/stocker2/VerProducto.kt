package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.stocker2.databinding.ActivityVerProductoBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore

class VerProducto : AppCompatActivity() {
    private lateinit var binding: ActivityVerProductoBinding
    private lateinit var btn_atras: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("Productos")
    private val myCollectionu = db.collection("usuarios")
    private var userID: String? = null
    private var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }

        // Inicializar SharedPreferences
        val sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        userID = sharedPreferences.getString("userId", null)

        val inte: Intent = intent
        val nombreProducto = inte.getStringExtra("NombreProducto").toString()

        obtenerIdDelProductoPorNombre(nombreProducto) { productId ->
            if (productId == null) {
                ToastUtil.showCustomToast(this, "Error al encontrar el producto")
                finish()
            } else {
                this.productId = productId
                checkIfFavorite(productId)
                cargarDetallesProducto(productId)
            }
        }
        binding.btnSupersConProducto.setOnClickListener {
            val intent = Intent(this, SupersConProducto::class.java)
            intent.putExtra("productoId", productId)
            startActivity(intent)
        }
    }

    private fun checkIfFavorite(productId: String) {
        userID?.let { uid ->
            myCollectionu.document(uid).get().addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val favoritos = documento.data?.get("favoritos") as? List<*>
                    val favoritosList = favoritos?.filterIsInstance<String>() ?: listOf()
                    if (productId in favoritosList) {
                        binding.imgStar.setImageResource(R.drawable.star_full)
                        binding.imgStar.tag = "full"
                    } else {
                        binding.imgStar.setImageResource(R.drawable.star_blank)
                        binding.imgStar.tag = "empty"
                    }
                } else {
                    binding.imgStar.setImageResource(R.drawable.star_blank)
                    binding.imgStar.tag = "empty"
                }
                binding.imgStar.isVisible = true
            }
        }
    }

    private fun toggleFavorite() {
        userID?.let { uid ->
            myCollectionu.document(uid).get().addOnSuccessListener { documento ->
                val favoritos = documento.data?.get("favoritos") as? MutableList<*>
                val favoritosList = favoritos?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                if (productId != null) {
                    if (productId in favoritosList) {
                        favoritosList.remove(productId)
                        binding.imgStar.setImageResource(R.drawable.star_blank)
                        binding.imgStar.tag = "empty"
                    } else {
                        favoritosList.add(productId!!)
                        binding.imgStar.setImageResource(R.drawable.star_full)
                        binding.imgStar.tag = "full"
                    }
                    myCollectionu.document(uid).update("favoritos", favoritosList)
                }
            }
        }
    }

    private fun obtenerIdDelProductoPorNombre(nombreProducto: String, callback: (String?) -> Unit) {
        myCollection.whereEqualTo("nombre", nombreProducto).limit(1).get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    callback(null)
                } else {
                    callback(documentos.documents.first().id)
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener el producto: ${e.message}")
                callback(null)
            }
    }

    private fun cargarDetallesProducto(productId: String) {
        myCollection.document(productId).get().addOnSuccessListener { documento ->
            binding.tvcal.text = getString(R.string.calorias_text, documento.getDouble("calorias").toString())
            binding.tvgram.text = getString(R.string.gramos_text, documento.getDouble("gramos").toString())
            binding.tvsano.text = getString(R.string.indicesan_text, documento.getLong("indiceSano").toString())
            Glide.with(this).load(documento.getString("urlImagen")).into(binding.imgProducto)
            binding.imgStar.setOnClickListener {
                toggleFavorite()
            }
        }.addOnFailureListener {
            ToastUtil.showCustomToast(this, "Error al obtener detalles del producto")
        }
    }

    private fun crearObjetosDelXml() {
        binding = ActivityVerProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
