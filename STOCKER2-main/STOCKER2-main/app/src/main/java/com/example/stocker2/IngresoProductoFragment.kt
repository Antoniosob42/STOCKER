package com.example.stocker2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.example.stocker2.databinding.FragmentIngresoProductoBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class IngresoProductoFragment : Fragment() {

    private var _binding: FragmentIngresoProductoBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView
    private lateinit var btnsubmit: Button
    private lateinit var textcal: TextView
    private lateinit var textgram: TextView
    private lateinit var textis: TextView
    private lateinit var textstock: TextView
    private lateinit var productoSpinner: Spinner
    private lateinit var numberPicker: NumberPicker
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
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
        arguments?.let {
            idSuper = it.getString("id")!!
        }

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let { uri ->
                    uploadImageToFirebase(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngresoProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    val productosStock = documento.data?.get("productosStock") as? List<*> ?: emptyList<Any>()
                    val productosStockList = productosStock.filterIsInstance<Map<String, Any>>()
                    if (productosStockList.isEmpty()) {
                        myCollections.document(idSuper).update("productosStock", listOf<Map<String, Any>>())
                        callback(-1)
                    } else {
                        val producto = productosStockList.find { it["idProducto"] == idProducto }
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

    private fun initUI() {
        imageView = binding.imgProducto
        textcal = binding.tvcal
        textgram = binding.tvgram
        textis = binding.tvsano
        textstock = binding.tvtustock
        productoSpinner = binding.productopicker
        numberPicker = binding.etCantProd
        btnsubmit = binding.btnGuardarProducto

        textcal.isVisible = false
        textgram.isVisible = false
        textis.isVisible = false
        textstock.isVisible = false

        btnsubmit.setOnClickListener {
            val selectedNumber = (numberPicker.value - 10)

            val selectedProductName = productoSpinner.selectedItem.toString()
            obtenerIdDelProductoPorNombre(selectedProductName) { productId ->
                if (productId == null) {
                    ToastUtil.showCustomToast(requireContext(), "No se encontró el producto.")
                    return@obtenerIdDelProductoPorNombre
                }

                cantidadDeProductoEnSupermercado(productId) { currentAmount ->
                    if (currentAmount == -1 && selectedNumber > 0) {
                        val newProduct = mapOf("idProducto" to productId, "cantidad" to selectedNumber)
                        myCollections.document(idSuper).update("productosStock", FieldValue.arrayUnion(newProduct))
                        textstock.text = getString(R.string.cantidad_en_tu_stock, selectedNumber.toString())
                        setFragmentResult("productoActualizado", Bundle())
                    } else if (currentAmount == -1) {
                        ToastUtil.showCustomToast(requireContext(), "No puedes retirar un producto que no está en tu stock.")
                    } else {
                        val newQuantity = currentAmount + selectedNumber
                        if (newQuantity > 0) {
                            actualizarCantidadProducto(idSuper, productId, newQuantity) { success ->
                                textstock.text = getString(R.string.cantidad_en_tu_stock, newQuantity.toString())
                                setFragmentResult("productoActualizado", Bundle())
                            }
                        } else if (newQuantity == 0) {
                            actualizarCantidadProducto(idSuper, productId, 0) { success ->
                                textstock.text = getString(R.string.no_posees_producto_en_stock)
                                ToastUtil.showCustomToast(requireContext(), "Cantidad retirada correctamente.")
                                setFragmentResult("productoActualizado", Bundle())
                            }
                        } else {
                            ToastUtil.showCustomToast(requireContext(), "No puedes retirar más cantidad de la que tienes en stock.")
                        }
                    }
                }
            }
        }

        productoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cargarurlenimageView(nombresProductos[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun actualizarCantidadProducto(idSupermercado: String, idProducto: String, nuevaCantidad: Int, callback: (Boolean) -> Unit) {
        val supermercadoRef = myCollections.document(idSupermercado)
        supermercadoRef.get().addOnSuccessListener { documento ->
            val productosStock = documento.data?.get("productosStock") as? List<*> ?: emptyList<Any>()
            val productosStockList = productosStock.filterIsInstance<Map<String, Any>>()
            val updatedStock = productosStockList.map { producto ->
                if (producto["idProducto"] == idProducto) {
                    producto.toMutableMap().apply { put("cantidad", nuevaCantidad) }
                } else {
                    producto
                }
            }
            supermercadoRef.update("productosStock", updatedStock).addOnCompleteListener {
                ToastUtil.showCustomToast(requireContext(), "Cantidad actualizada correctamente")
                callback(true)
            }
        }.addOnFailureListener {
            ToastUtil.showCustomToast(requireContext(), "Error al actualizar el producto: ${it.message}")
            callback(false)
        }
    }

    private fun cargarurlenimageView(nombre: String) {
        productoUrlMap[nombre]?.let { url ->
            Glide.with(this@IngresoProductoFragment).load(url).into(imageView)
        } ?: run {
            ToastUtil.showCustomToast(requireContext(), "No se encontró la URL de la imagen para el producto seleccionado")
        }
        cargarvaloresproducto(nombre)
    }

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
                    activity?.runOnUiThread {
                        if (numproducto == -1) {
                            textstock.text = getString(R.string.no_posees_producto_en_stock)
                        } else {
                            textstock.text = getString(R.string.cantidad_en_tu_stock, numproducto.toString())
                        }
                    }
                }
            } else {
                activity?.runOnUiThread {
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

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresProductos)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            productoSpinner.adapter = adapter
        }.addOnFailureListener {
            ToastUtil.showCustomToast(requireContext(), "Error al obtener productos")
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("supermercados/${idSuper}/profile.jpg")
        storageRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateSupermercadoImage(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                ToastUtil.showCustomToast(requireContext(), "Error al subir imagen: ${e.localizedMessage}")
            }
    }

    private fun updateSupermercadoImage(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        idSuper.let { id ->
            db.collection("supermercados").document(id).update("urlImagen", imageUrl)
                .addOnSuccessListener {
                    loadImageWithGlide(imageUrl)
                    ToastUtil.showCustomToast(requireContext(), "Imagen actualizada correctamente")
                }
                .addOnFailureListener { e ->
                    ToastUtil.showCustomToast(requireContext(), "Error al actualizar la imagen: ${e.localizedMessage}")
                }
        }
    }

    private fun loadImageWithGlide(imageUrl: String) {
        val widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics).toInt()
        val heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()

        Glide.with(this)
            .load(imageUrl)
            .override(widthPx, heightPx)
            .into(imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(idSuper: String) =
            IngresoProductoFragment().apply {
                arguments = Bundle().apply {
                    putString("id", idSuper)
                }
            }
    }
}
