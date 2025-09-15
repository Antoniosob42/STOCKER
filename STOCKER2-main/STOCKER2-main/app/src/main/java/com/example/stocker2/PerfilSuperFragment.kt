package com.example.stocker2

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

private const val ARG_PARAM1 = "param1"

class PerfilSuperFragment : Fragment() {
    private var idSuper: String? = null
    private lateinit var imagePerfil: ImageView
    private lateinit var btnVideo: Button
    private lateinit var btnUbi: Button
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private val db = FirebaseFirestore.getInstance()
    private val myCollections = db.collection("supermercados")
    private lateinit var tvCiudad: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvPagWeb: TextView
    private lateinit var tvCorreo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idSuper = it.getString(ARG_PARAM1)
        }

        // Inicializar el ActivityResultLauncher
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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil_super, container, false)
        imagePerfil = view.findViewById(R.id.imagePerfil)
        imagePerfil.setOnClickListener {
            selectImage()
        }
        btnUbi = view.findViewById(R.id.btnUbi)
        btnVideo = view.findViewById(R.id.btnVideoProm)

        tvCiudad = view.findViewById(R.id.tvciudad)
        tvDireccion = view.findViewById(R.id.tvDir)
        tvPagWeb = view.findViewById(R.id.tvPagWeb)
        tvCorreo = view.findViewById(R.id.tvCorreo)

        tvCiudad.setOnClickListener { showEditDialog("Ciudad", tvCiudad) }
        tvDireccion.setOnClickListener { showInfoDialog(getString(R.string.direccion_no_actualizable)) }
        tvPagWeb.setOnClickListener { showEditDialog("paginaweb", tvPagWeb) }
        tvCorreo.setOnClickListener { showEditDialog("correo", tvCorreo) }

        btnVideo.setOnClickListener {
            val intent = Intent(requireContext(), SubirVideo::class.java)
            intent.putExtra("id", idSuper)
            startActivity(intent)
        }

        btnUbi.setOnClickListener {
            val intent = Intent(requireContext(), RegistroMapas::class.java)
            intent.putExtra("id", idSuper)
            startActivity(intent)
        }

        cargarDatosSupermercado(view)
        return view
    }

    private fun loadImageWithGlide(imageUrl: String) {
        val widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics).toInt()
        val heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()

        Glide.with(this)
            .load(imageUrl)
            .override(widthPx, heightPx) // Redimensiona la imagen
            .into(imagePerfil)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
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
        idSuper?.let { id ->
            db.collection("supermercados").document(id).update("urlImagen", imageUrl)
                .addOnSuccessListener {
                    loadImageWithGlide(imageUrl) // Usa el nuevo método para cargar la imagen redimensionada
                    ToastUtil.showCustomToast(requireContext(), "Imagen actualizada correctamente.")
                }
                .addOnFailureListener { e ->
                    ToastUtil.showCustomToast(requireContext(), "Error al actualizar la imagen: ${e.localizedMessage}")
                }
        }
    }

    private fun cargarDatosSupermercado(view: View) {
        idSuper?.let { id ->
            myCollections.document(id).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val ciudad = documento.getString("Ciudad") ?: getString(R.string.no_has_registrado_tu_ciudad)
                        val direccion = documento.getString("direccion") ?: getString(R.string.no_has_registrado_tu_direccion)
                        val paginaWeb = documento.getString("paginaweb") ?: getString(R.string.no_has_registrado_tu_pagina_web)
                        val correo = documento.getString("correo") ?: getString(R.string.no_has_registrado_tu_correo_electronico)
                        val nombre = documento.getString("nombre") ?: getString(R.string.nombre_no_disponible)
                        val urlImagen = documento.getString("urlImagen") ?: ""

                        // Seteando los textos y la imagen
                        tvCiudad.text = getString(R.string.tu_ciudad, ciudad)
                        tvDireccion.text = getString(R.string.tu_direccion, direccion)
                        tvPagWeb.text = getString(R.string.tu_pagina_web, paginaWeb)
                        tvCorreo.text = getString(R.string.tu_correo_electronico, correo)
                        view.findViewById<TextView>(R.id.tvName).text = nombre

                        if (urlImagen.isNotEmpty()) {
                            Glide.with(this).load(urlImagen).into(imagePerfil)
                        } else {
                            imagePerfil.setImageResource(R.drawable.carrito) // Asumiendo que tienes una imagen predeterminada
                        }
                    } else {
                        ToastUtil.showCustomToast(requireContext(), "No se encontraron datos del supermercado.")
                    }
                }
                .addOnFailureListener { e ->
                    ToastUtil.showCustomToast(requireContext(), "Error al cargar los datos: ${e.localizedMessage}")
                }
        }
    }

    private fun showEditDialog(campo: String, textView: TextView) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.actualizar_campo, campo))

        val input = EditText(requireContext())
        input.setText(textView.text.toString().substringAfter(": ").trim())

        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val nuevoValor = input.text.toString()
            textView.text = getString(R.string.tu_campo, campo, nuevoValor)
            idSuper?.let { id ->
                myCollections.document(id).update(campo.lowercase(), nuevoValor)
                    .addOnSuccessListener {
                        ToastUtil.showCustomToast(requireContext(), getString(R.string.campo_actualizado_correctamente, campo))
                    }
                    .addOnFailureListener { e ->
                        ToastUtil.showCustomToast(requireContext(), getString(R.string.error_al_actualizar_campo, campo, e.localizedMessage))
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showInfoDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(idSuper: String) =
            PerfilSuperFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, idSuper)
                }
            }
    }
}
