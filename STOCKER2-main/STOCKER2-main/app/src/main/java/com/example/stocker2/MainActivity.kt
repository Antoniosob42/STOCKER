package com.example.stocker2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.stocker2.databinding.ActivityMainBinding
import com.example.stocker2.utils.ToastUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Actividad principal de la aplicación Stocker2.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2

    /**
     * Se llama cuando la actividad está iniciando.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isUser = sharedPreferences.getBoolean("isUser", true)
        if (!isLoggedIn) {
            // Si no está logueado, redirige a la pantalla de inicio de sesión
            if (isUser) {
                val intent = Intent(this, InicioSesionUsuario::class.java)
                startActivity(intent)
                finish()
                return
            }
        } else if (!isUser) {
            val intent = Intent(this, MainActivitySuperMercado::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Inflar el diseño utilizando View Binding
        crearObjetosDelXML()
        // Configurar la ActionBar
        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appbar.btnAtras.isVisible = false

        // Configurar el ViewPager2
        viewPager = binding.viewPager!!
        viewPager.adapter = ViewPagerAdapter(this)

        // Configurar el BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_user -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_allProduct_user -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_searchProduct_user -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_searchSuperMarket -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }

        // Sincronizar el ViewPager2 con el BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> navView.selectedItemId = R.id.nav_home_user
                    1 -> navView.selectedItemId = R.id.nav_allProduct_user
                    2 -> navView.selectedItemId = R.id.nav_searchProduct_user
                    3 -> navView.selectedItemId = R.id.nav_searchSuperMarket
                }
            }
        })

        // Seleccionar el primer ítem al iniciar
        navView.selectedItemId = R.id.nav_home_user

        // Obtener el token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                ToastUtil.showCustomToast(this, "Error al obtener el token de FCM")
                return@addOnCompleteListener
            }

            val token = task.result
            val editor = sharedPreferences.edit()
            editor.putString("fcm_token", token)
            editor.apply()
        }

    }

    /**
     * Infla el diseño utilizando View Binding.
     */
    private fun crearObjetosDelXML() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Crea el menú de opciones en la ActionBar.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main3, menu)
        return true
    }

    /**
     * Maneja la selección de elementos del menú.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.AcDe -> {
                // Abrir la actividad 'AcercaDeActivity'
                val intent = Intent(this, AcercaDeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.Preferencias -> {
                // Abrir la actividad 'PREFERENCIAS'
                val intent = Intent(this, PREFERENCIAS::class.java)
                startActivity(intent)
                true
            }
            R.id.cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun cerrarSesion() {
        val editor = sharedPreferences.edit()
        editor.remove("isLoggedIn")
        editor.remove("userId")
        editor.apply()

        // Redirige al usuario a la pantalla de inicio de sesión
        val intent = Intent(this, InicioSesionUsuario::class.java)
        startActivity(intent)
        finish()
    }

}
