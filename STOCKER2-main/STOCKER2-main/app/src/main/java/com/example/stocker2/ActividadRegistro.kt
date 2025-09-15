package com.example.stocker2

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.stocker2.databinding.LayoutRegistroBinding
import com.example.stocker2.utils.ToastUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.security.MessageDigest

/**
 * [ActividadRegistro] es una actividad que permite a los usuarios registrar nuevos supermercados.
 */
class ActividadRegistro : AppCompatActivity() {

    private lateinit var btn_atras: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val myCollection = db.collection("supermercados")

    private lateinit var binding: LayoutRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearObjetosDelXml()

        binding.btnRegReg.setOnClickListener {
            btguardarRegistro()
        }

        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_atras = findViewById(R.id.btn_atras)
        btn_atras.setOnClickListener {
            finish()
        }

        // Agregar OnLongClickListener para ETNomEmpr
        binding.ETNomEmpr.setOnLongClickListener {
            openContextMenu(binding.ETNomEmpr)
            true
        }

        // Agregar OnLongClickListener para ETCiuEmpr
        binding.ETCiuEmpr.setOnLongClickListener {
            openContextMenu(binding.ETCiuEmpr)
            true
        }

        registerForContextMenu(binding.ETNomEmpr)
        registerForContextMenu(binding.ETCiuEmpr)
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v?.id) {
            R.id.ETNomEmpr -> menuInflater.inflate(R.menu.menu_contextual_sucursales, menu)
            R.id.ETCiuEmpr -> menuInflater.inflate(R.menu.menu_contextual_ciudades, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Opciones para ETNomEmpr
            R.id.option_mercadona -> binding.ETNomEmpr.setText(R.string.mercadona)
            R.id.option_dia -> binding.ETNomEmpr.setText(R.string.dia)
            R.id.option_lidl -> binding.ETNomEmpr.setText(R.string.lidl)
            R.id.option_carrefour -> binding.ETNomEmpr.setText(R.string.carrefour)
            R.id.option_eroski -> binding.ETNomEmpr.setText(R.string.eroski)
            R.id.option_consum -> binding.ETNomEmpr.setText(R.string.consum)

            // Opciones para ETCiuEmpr
            R.id.option_ciudad_real -> binding.ETCiuEmpr.setText(R.string.ciudad_real)
            R.id.option_madrid -> binding.ETCiuEmpr.setText(R.string.madrid)
            R.id.option_almagro -> binding.ETCiuEmpr.setText(R.string.almagro)
            R.id.option_puertollano -> binding.ETCiuEmpr.setText(R.string.puertollano)
            R.id.option_pozuelo -> binding.ETCiuEmpr.setText(R.string.pozuelo)
            R.id.option_daimiel -> binding.ETCiuEmpr.setText(R.string.daimiel)
            R.id.option_tomelloso -> binding.ETCiuEmpr.setText(R.string.tomelloso)
            R.id.option_sevilla -> binding.ETCiuEmpr.setText(R.string.sevilla)
            R.id.option_cordoba -> binding.ETCiuEmpr.setText(R.string.cordoba)
            R.id.option_granada -> binding.ETCiuEmpr.setText(R.string.granada)

            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    private fun crearObjetosDelXml() {
        binding = LayoutRegistroBinding.inflate(layoutInflater)
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
     * Método que guarda el nuevo registro de supermercado en Firebase Firestore.
     */
    fun btguardarRegistro() {
        val nombreEmpresa = binding.ETNomEmpr.text.toString()
        val contrasena = binding.ETID.text.toString()
        val direccion = binding.ETDir.text.toString()

        if (nombreEmpresa.isEmpty() || contrasena.isEmpty() || direccion.isEmpty()) {
            resultadoOperacion("El nombre de la empresa, la contraseña y la dirección son obligatorios")
            return
        }

        val contrasenaHashed = hashPassword(contrasena)

        myCollection
            .whereEqualTo("nombre", nombreEmpresa)
            .whereEqualTo("direccion", direccion)
            .get()
            .addOnSuccessListener { existingSupermarketSnapshot ->
                if (!existingSupermarketSnapshot.isEmpty) {
                    resultadoOperacion("Este supermercado con esta dirección ya está registrado.")
                } else {
                    myCollection
                        .orderBy("id", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { latestSupermarketSnapshot ->
                            val nuevoId = if (!latestSupermarketSnapshot.isEmpty) {
                                val ultimoSupermercado = latestSupermarketSnapshot.documents[0]
                                val ultimoId = ultimoSupermercado.getLong("id")
                                ultimoId?.toInt()?.plus(1) ?: 1
                            } else {
                                1
                            }

                            val data = hashMapOf(
                                "id" to nuevoId,
                                "nombre" to nombreEmpresa,
                                "Contraseña" to contrasenaHashed,
                                "Ciudad" to binding.ETCiuEmpr.text.toString(),
                                "direccion" to direccion,
                                "correo" to binding.ETTLF.text.toString(),
                                "paginaweb" to binding.ETPWBEmp.text.toString(),
                                "urlImagen" to ""
                            )

                            myCollection
                                .document(nuevoId.toString())
                                .set(data)
                                .addOnSuccessListener {
                                    resultadoOperacion("Registro guardado correctamente")
                                    finish()
                                }
                        }
                }
            }
    }

    /**
     * Método que muestra un mensaje de resultado de la operación.
     */
    private fun resultadoOperacion(mensaje: String) {
        // Limpiar campos de entrada
        binding.ETID.setText("")
        binding.ETCiuEmpr.setText("")
        binding.ETNomEmpr.setText("")
        binding.ETPWBEmp.setText("")
        binding.ETTLF.setText("")
        ToastUtil.showCustomToast(this, mensaje)
    }
}
