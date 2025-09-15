package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.stocker2.databinding.LayoutInicioSesionBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

/**
 * [ActividadInicioSesion] es una actividad que permite a los usuarios iniciar sesión.
 */
class ActividadInicioSesion : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: LayoutInicioSesionBinding
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("supermercados")
    private lateinit var btn_atras: ImageView
    private var bolguardo = false

    /**
     * Método llamado cuando se crea la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialización de los elementos del diseño XML
        crearObjetosDelXml()

        // Obtención de las preferencias compartidas
        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        val siono = sharedPreferences.getString("boolgr", "no")

        if (siono == "si") {
            bolguardo = true
            Log.d("controlis", " la preferencia se ha actualizado")
        } else {
            // Restablecer preferencias si no se encontró "boolgr" o es "no"
            val editor = sharedPreferences.edit()
            editor.putString("nombrealmacenado", "")
            editor.putString("contrasenaalmacenada", "")
            editor.putString("direccionalmacenada", "")
            editor.apply()
        }

        // Actualizar campos de entrada con valores guardados si "boolgr" es "si"
        if (bolguardo) {
            val txtnom = sharedPreferences.getString("nombrealmacenado", "")
            val txtcon = sharedPreferences.getString("contrasenaalmacenada", "")
            val txtdir = sharedPreferences.getString("direccionalmacenada", "")
            binding.ETISN.setText(txtnom)
            binding.etISC.setText(txtcon)
            binding.etOpcDir.setText(txtdir)
            Log.d("controlis", " se supone que se han actualizado los registros así: nombre= $txtnom, contraseña= $txtcon, dirección= $txtdir")
        }

        // Configuración de visibilidad e interactividad del boton de registro


        // Configuración de un listener para el botón de inicio de sesión
        binding.btnIniSec.setOnClickListener {
            btInicioSesion()
        }

        // Configuración de la barra de acción y del botón de retroceso
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
        binding.BTNABREINIUSU.setOnClickListener{
            val intent = Intent(this,InicioSesionUsuario::class.java)
            startActivity(intent)
        }
    }
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    /**
     * Método que inicializa la vinculación con los elementos de diseño XML.
     */
    private fun crearObjetosDelXml() {
        binding = LayoutInicioSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Método llamado para crear el menú de opciones en la barra de acción.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main2, menu)
        return true
    }

    /**
     * Método llamado cuando se hace clic en un elemento del menú de opciones.
     */
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

    /**
     * Método llamado al hacer clic en el botón de inicio de sesión.
     */
    fun btInicioSesion() {
        val nombreEmpresa: String = binding.ETISN.text.toString()
        val contrasena: String = binding.etISC.text.toString()
        val direccion: String = binding.etOpcDir.text.toString()

        if (nombreEmpresa.isEmpty() || contrasena.isEmpty()) {
            resultadoOperacion("Ni el nombre de la empresa ni la contraseña pueden estar vacíos")
            return
        }

        if (direccion.isEmpty()) {
            resultadoOperacion("Necesitas ingresar una dirección para buscar la empresa")
            return
        }
        verificarEmpresa(nombreEmpresa, contrasena, direccion)
    }

    /**
     * Método que verifica la existencia de la empresa en Firebase Firestore.
     */
    private fun verificarEmpresa(nombreEmpresa: String, contrasena: String, direccion: String) {
        val contrasenaCifrada = hashPassword(contrasena)

        myCollection
            .whereEqualTo("nombre", nombreEmpresa)
            .whereEqualTo("direccion", direccion)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val found = querySnapshot.documents.any {
                    val passwordStored = it.getString("Contraseña")
                    passwordStored != null && passwordStored == contrasenaCifrada
                }

                if (found) {
                    val documento = querySnapshot.documents.first()
                    val idAbuscar = documento.getLong("id")?.toString() ?: ""  // Convertir Long a String aquí
                    val editor = sharedPreferences.edit()
                    if (bolguardo) {
                        editor.putString("nombrealmacenado", nombreEmpresa)
                        editor.putString("contrasenaalmacenada", contrasena)
                        editor.putString("direccionalmacenada", direccion)
                        editor.apply()
                    }

                    val valorpreferences = sharedPreferences.getString("nombrealmacenado", "")
                    Log.d("controlis", " se presupone que este es el nombre almacenado $valorpreferences")
                    obtenerDatosEmpresa(idAbuscar)
                } else {

                    resultadoOperacion("No se encontró ninguna empresa con esos datos, ¿deseas registrarte?")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreData", "Error al verificar la empresa: ${e.message}")
                resultadoOperacion("Error al conectar con la base de datos.")
            }
    }

    /**
     * Método que obtiene información adicional de la empresa y redirige al usuario.
     */
    private fun obtenerDatosEmpresa(idEmpresa: String) {
        myCollection.document(idEmpresa).get()
            .addOnSuccessListener {

                val id: String = it.get("id").toString()

                resultadoOperacion("¡Bienvenido!")
                val intent = Intent(this, MainActivitySuperMercado::class.java)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("marketId", id)
                editor.putBoolean("isUser",false)
                editor.apply()
                startActivity(intent)
            }
    }

    /**
     * Método que muestra un mensaje de resultado de la operación.
     */
    private fun resultadoOperacion(mensaje: String) {
        if(!bolguardo) {
            binding.ETISN.setText("")
            binding.etISC.setText("")
            binding.etOpcDir.setText("")
        }
        ToastUtil.showCustomToast(this, mensaje)
    }

    /**
     * Método llamado al hacer clic en el botón "Registrarse".
     */
    fun abrirRegistrar(view: View) {
        val intent = Intent(this, ActividadRegistro::class.java)
        startActivity(intent)
    }

}