package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.stocker2.databinding.ActivityInicioSesionUsuarioBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class InicioSesionUsuario : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityInicioSesionUsuarioBinding
    private val myCollection = db.collection("usuarios")
    private lateinit var btn_atras: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosXml()

        // Inicialización de sharedPreferences
        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)


        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }

        binding.btnAbreIniEmp.setOnClickListener{
            val intent = Intent(this, ActividadInicioSesion::class.java)
            startActivity(intent)
        }

        binding.btnAbreRegUsu.setOnClickListener{
            val intent = Intent(this, RegistroUsuario::class.java)
            startActivity(intent)
        }

        binding.btnIniSesUsu.setOnClickListener {
            comprobarInicioSesion()
        }
    }


    private fun crearObjetosXml() {
        binding = ActivityInicioSesionUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    private fun comprobarInicioSesion() {
        val nombrePuesto:String=binding.etnombUsu.text.toString()
        val contraPuesta:String=binding.etpassUsu.text.toString()
        if(nombrePuesto.isEmpty()||contraPuesta.isEmpty()){
            resultadoOperacion("Debes Rellenar todos los datos")
        }
        else{
            comprobarAcceso(nombrePuesto,contraPuesta)
        }
    }

    private fun comprobarAcceso(nombrePuesto: String, contraPuesta: String) {
        val contraCifrada = hashPassword(contraPuesta)

        myCollection.whereEqualTo("Nusuario", nombrePuesto)
            .get()
            .addOnSuccessListener { entrada ->
                val foundDocument = entrada.documents.firstOrNull {
                    val passwordusu = it.getString("Contraseña")
                    passwordusu != null && passwordusu == contraCifrada
                }

                if (foundDocument != null) {
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.putString("userId", foundDocument.id) // Guardar ID del usuario si necesario
                    editor.putBoolean("isUser",true)
                    editor.apply()

                    // Navega a la siguiente actividad
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    resultadoOperacion("Usuario o contraseña incorrectos")
                }
            }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main2, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.AcDe -> {
                // Abrir la actividad AcercaDeActivity
                val intent = Intent(this, AcercaDeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun resultadoOperacion(mensaje: String) {

            binding.etnombUsu.setText("")
            binding.etpassUsu.setText("")

        ToastUtil.showCustomToast(this, mensaje)
    }
}