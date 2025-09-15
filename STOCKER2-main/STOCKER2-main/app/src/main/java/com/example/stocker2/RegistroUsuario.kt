package com.example.stocker2

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.stocker2.databinding.ActivityRegistroUsuarioBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class RegistroUsuario : AppCompatActivity() {
    private lateinit var btn_atras: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("usuarios")

    private lateinit var binding: ActivityRegistroUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
        binding.btnRegUsu.setOnClickListener{
            comprobarCredenciales()
        }

    }
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun comprobarCredenciales() {
        val nombUsu: String = binding.ETNomUsu.text.toString().trim()
        val contraUsu: String = binding.etRPassUsu.text.toString().trim()
        val emailUsu: String = binding.etREmailUsu.text.toString().trim()

        if (nombUsu.isEmpty() || contraUsu.isEmpty() || emailUsu.isEmpty()) {
            resultadoOperacion("Se deben rellenar todos los campos para registrarse.")
            return
        }

        // Primero, verifica si el email o el nombre de usuario ya están registrados
        myCollection.whereEqualTo("email", emailUsu)
            .get()
            .continueWithTask { task ->
                if (!task.isSuccessful || !task.result.isEmpty) {
                    resultadoOperacion("El email ya esta registrado.")
                }
                myCollection.whereEqualTo("Nusuario", nombUsu).get()
            }
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    resultadoOperacion("El snombre de usuario ya está registrado.")
                } else {
                    // Si no hay registros duplicados, procede a registrar el nuevo usuario
                    registrarNuevoUsuario(nombUsu, emailUsu, contraUsu)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                resultadoOperacion(exception.message ?: "Error al verificar la información del usuario.")
            }
    }

    private fun registrarNuevoUsuario(nombUsu: String, emailUsu: String, contraUsu: String) {
        val contraHashed = hashPassword(contraUsu)
        val nuevoUsuario = hashMapOf(
            "Nusuario" to nombUsu,
            "email" to emailUsu,
            "Contraseña" to contraHashed
        )

        myCollection.add(nuevoUsuario)
            .addOnSuccessListener {
                resultadoOperacion("Usuario registrado correctamente.")
            }
            .addOnFailureListener {
                resultadoOperacion("Error al registrar el usuario.")
            }
    }
    private fun resultadoOperacion(mensaje: String) {

        binding.etRPassUsu.setText("")
        binding.ETNomUsu.setText("")
        binding.etREmailUsu.setText("")
        ToastUtil.showCustomToast(this, mensaje)

    }
    private fun crearObjetosDelXml() {
        binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}