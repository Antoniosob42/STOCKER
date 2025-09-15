package com.example.stocker2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.stocker2.databinding.ActivityIngresoProductosBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ActivityIngresoProductos : AppCompatActivity() {

    private lateinit var binding: ActivityIngresoProductosBinding
    private lateinit var btn_atras: ImageView
    private lateinit var imageView: ImageView
    private lateinit var btnsubmit: Button
    private lateinit var textcal: TextView
    private lateinit var textgram: TextView
    private lateinit var textis: TextView
    private lateinit var textstock: TextView
    private lateinit var productoSpinner: Spinner
    private lateinit var numberPicker: NumberPicker
    private val nombresProductos = ArrayList<String>()
    private val db = FirebaseFirestore.getInstance()
    private val myCollectionp = db.collection("Productos")
    private val myCollections = db.collection("supermercados")
    private var idSuper: String = ""
    private val productoUrlMap = mutableMapOf<String, String>()
    private val productoCaloriasMap = mutableMapOf<String, String>()
    private val productoGramosMap = mutableMapOf<String, String>()
    private val productoIndiceSanoMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngresoProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val objIntent: Intent = intent
        idSuper = objIntent.getStringExtra("id")!!

        initUI()
        cargarNombresProductosEnSpinner()
        numberPicker.minValue = 0
        numberPicker.maxValue = 20
        numberPicker.displayedValues = arrayOf("-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        numberPicker.value = 10
    }

    private fun cantidadDeProductoEnSupermercado(idProducto: String, callback: (Int) -> Unit) {
        myCollections.document(idSuper).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val productosStock = documento.data?.get("productosStock") as? List<*>
                    val productosStockList = productosStock?.filterIsInstance<Map<String, Any>>() ?: emptyList()

                    if (productosStockList.isEmpty()) {
                        myCollections.document(idSuper).update("productosStock", listOf<Map<String, Any>>())
                        callback(-1)
                    } else {
                        val producto = productosStockList.find { producto ->
                            producto["idProducto"] == idProducto
                        }
                        if (producto != null) {
                            callback((producto["cantidad"] as? Number)?.toInt() ?: -1)
                        } else {
                            callback(-1)
                        }
                    }
                } else {
                    callback(-1)
                }
            }
            .addOnFailureListener {
                println("Error al obtener datos: ${it.message}")
                callback(-1)
            }
    }

    private fun obtenerIdDelProductoPorNombre(nombreProducto: String, callback: (String?) -> Unit) {
        myCollectionp
            .whereEqualTo("nombre", nombreProducto)
            .limit(1)
            .get()
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

    @SuppressLint("StringFormatMatches")
    private fun initUI() {
        btn_atras = findViewById(R.id.btn_atras)
        imageView = findViewById(R.id.imgProducto)
        textcal = findViewById(R.id.tvcal)
        textgram = findViewById(R.id.tvgram)
        textis = findViewById(R.id.tvsano)
        textstock = findViewById(R.id.tvtustock)
        productoSpinner = findViewById(R.id.productopicker)
        numberPicker = findViewById(R.id.etCantProd)
        btnsubmit = findViewById(R.id.btnGuardarProducto)

        btnsubmit.setOnClickListener {
            val selectedNumber = (numberPicker.value - 10)
            val selectedProductName = productoSpinner.selectedItem.toString()
            obtenerIdDelProductoPorNombre(selectedProductName) { productId ->
                if (productId == null) {
                    ToastUtil.showCustomToast(this, "No se encontró el producto")
                    return@obtenerIdDelProductoPorNombre
                }
                cantidadDeProductoEnSupermercado(productId) { currentAmount ->
                    if (currentAmount == -1 && selectedNumber > 0) {
                        val newProduct = mapOf("idProducto" to productId, "cantidad" to selectedNumber)
                        myCollections.document(idSuper).update("productosStock", FieldValue.arrayUnion(newProduct))
                        textstock.text = getString(R.string.cantidad_en_tu_stock, selectedNumber)
                    } else {
                        val newQuantity = currentAmount + selectedNumber
                        if (newQuantity > 0) {
                            actualizarCantidadProducto(idSuper, productId, newQuantity) { success ->
                                textstock.text = getString(R.string.cantidad_en_tu_stock, newQuantity)
                            }
                        } else {
                            eliminarProductoDelStock(idSuper, productId) { success ->
                                textstock.text = getString(R.string.no_posees_producto_en_stock)
                            }
                        }
                    }
                }
            }
        }

        textcal.isVisible = false
        textgram.isVisible = false
        textis.isVisible = false
        textstock.isVisible = false

        btn_atras.setOnClickListener {
            finish()
        }

        productoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cargarurlenimageView(nombresProductos[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarCantidadProducto(idSupermercado: String, idProducto: String, nuevaCantidad: Int, callback: (Boolean) -> Unit) {
        val supermercadoRef = myCollections.document(idSupermercado)
        supermercadoRef.get().addOnSuccessListener { documento ->
            val productosStock = documento.data?.get("productosStock") as? List<*>
            val productosStockList = productosStock?.filterIsInstance<Map<String, Any>>() ?: emptyList()

            val updatedStock = productosStockList.map { producto ->
                if (producto["idProducto"] == idProducto) {
                    producto.toMutableMap().apply { put("cantidad", nuevaCantidad) }
                } else {
                    producto
                }
            }
            supermercadoRef.update("productosStock", updatedStock).addOnCompleteListener {
                ToastUtil.showCustomToast(this, "Cantidad Añadida Correctamente")
                callback(true)
            }
        }.addOnFailureListener {
            ToastUtil.showCustomToast(this, "Error al actualizar el producto: ${it.message}")
            callback(false)
        }
    }

    private fun eliminarProductoDelStock(idSupermercado: String, idProducto: String, callback: (Boolean) -> Unit) {
        val supermercadoRef = myCollections.document(idSupermercado)
        supermercadoRef.get().addOnSuccessListener { documento ->
            val productosStock = documento.data?.get("productosStock") as? List<*>
            val productosStockList = productosStock?.filterIsInstance<Map<String, Any>>() ?: emptyList()

            val updatedStock = productosStockList.filterNot { producto ->
                producto["idProducto"] == idProducto
            }
            supermercadoRef.update("productosStock", updatedStock).addOnCompleteListener {
                ToastUtil.showCustomToast(this, "Producto Retirado Correctamente")
                callback(false)
            }
        }.addOnFailureListener {
            ToastUtil.showCustomToast(this, "Error al eliminar el producto: ${it.message}")
            callback(false)
        }
    }

    private fun cargarurlenimageView(nombre: String) {
        productoUrlMap[nombre]?.let { url ->
            Glide.with(this@ActivityIngresoProductos).load(url).into(imageView)
        } ?: run {
            ToastUtil.showCustomToast(this, "No se encontró la URL de la imagen para el producto seleccionado")
        }
        cargarvaloresproducto(nombre)
    }

    @SuppressLint("StringFormatMatches")
    private fun cargarvaloresproducto(nombre: String) {
        textcal.isVisible = true
        textgram.isVisible = true
        textis.isVisible = true
        textstock.isVisible = true

        productoCaloriasMap[nombre]?.let { calorias ->
            textcal.text = getString(R.string.calorias_text, calorias)
        }
        productoGramosMap[nombre]?.let { gramos ->
            textgram.text = getString(R.string.gramos_text, gramos)
        }
        productoIndiceSanoMap[nombre]?.let { indice ->
            textis.text = getString(R.string.indicesan_text, indice)
        }

        obtenerIdDelProductoPorNombre(nombre) { id ->
            if (id != null) {
                cantidadDeProductoEnSupermercado(id) { numproducto ->
                    runOnUiThread {
                        if (numproducto == -1) {
                            textstock.text = getString(R.string.no_posees_producto_en_stock)
                        } else {
                            textstock.text = getString(R.string.cantidad_en_tu_stock, numproducto)
                        }
                    }
                }
            } else {
                runOnUiThread {
                    textstock.text = getString(R.string.producto_no_registrado)
                }
            }
        }
    }

    private fun cargarNombresProductosEnSpinner() {
        myCollectionp.get().addOnSuccessListener { documentos ->
            nombresProductos.clear()
            productoUrlMap.clear()
            productoCaloriasMap.clear()
            productoGramosMap.clear()
            productoIndiceSanoMap.clear()

            for (documento in documentos) {
                documento.getString("nombre")?.let { nombre ->
                    nombresProductos.add(nombre)
                    documento.getString("urlImagen")?.let { url ->
                        productoUrlMap[nombre] = url
                    }
                    documento.getDouble("calorias")?.let { calorias ->
                        productoCaloriasMap[nombre] = calorias.toString()
                    }
                    documento.getDouble("gramos")?.let { gramos ->
                        productoGramosMap[nombre] = gramos.toString()
                    }
                    documento.getLong("indiceSano")?.let { indiceSano ->
                        productoIndiceSanoMap[nombre] = indiceSano.toString()
                    }
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresProductos)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            productoSpinner.adapter = adapter
        }.addOnFailureListener {
            ToastUtil.showCustomToast(this, "Error al obtener Productos")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_act1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val objIntent: Intent = intent
        val PagWeb = objIntent.getStringExtra("PaginaWeb")
        val email = objIntent.getStringExtra("correo")
        return when (item.itemId) {
            R.id.Web -> {
                if (PagWeb != null) {
                    abrirPagina(PagWeb)
                }
                true
            }
            R.id.Contactaremail -> {
                if (email != null) {
                    mandarCorreo(email)
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

    private fun abrirPagina(PagWeb: String) {
        if (PagWeb.isNotEmpty()) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PagWeb)))
        } else {
            ToastUtil.showCustomToast(this, "No introdujiste ninguna página web al registrarte")
        }
    }

    private fun mandarCorreo(email: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Contacto a empresa")
            putExtra(Intent.EXTRA_TEXT, "")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        })
    }
}
