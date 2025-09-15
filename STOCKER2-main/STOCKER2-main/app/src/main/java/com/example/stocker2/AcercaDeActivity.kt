package com.example.stocker2

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.stocker2.databinding.ActivityAcercaDeBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * es una actividad que muestra información sobre la aplicación.

 */
class AcercaDeActivity : AppCompatActivity() {

    // Instancia de Firebase Firestore
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("ACERCADE")

    // Vinculación con los elementos de diseño XML mediante View Binding
    lateinit var binding: ActivityAcercaDeBinding

    // Elemento de la interfaz de usuario para el botón de retroceso
    private lateinit var btn_atras: ImageView

    /**
     * Método llamado cuando se crea la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialización de los elementos del diseño XML
        crearObjetosDelXml()

        // Obtención del idioma del dispositivo
        val idioma: String = idioma()

        // Obtención del texto de la colección "ACERCADE" según el idioma y su visualización
        myCollection
            .document(idioma)
            .get()
            .addOnSuccessListener { result ->
                val texto = result.getString("texto")
                val textView = TextView(this)
                textView.text = texto
                // Establece el color del texto en hexadecimal (#B68D8D)
                textView.setTextColor(Color.parseColor("#000000"))
                textView.setTextSize(20F)
                binding.LinearLayout.addView(textView)
            }

        // Configuración de la barra de acción y del botón de retroceso
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }
    }

    /**
     * Método que devuelve el idioma del dispositivo.
     *
     * @return [String] - El idioma actual del dispositivo.
     */
    private fun idioma(): String {
        val currentLocale = Locale.getDefault()
        val language = currentLocale.language
        return when (language) {
            "es" -> "espanol"
            "fr" -> "frances"
            "de" -> "aleman"
            else -> "ingles"
        }
    }

    /**
     * Método que inicializa la vinculación con los elementos de diseño XML.
     */
    private fun crearObjetosDelXml() {
        binding = ActivityAcercaDeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}