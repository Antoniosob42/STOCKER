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
import com.example.stocker2.databinding.ActivityMainSuperMercadoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivitySuperMercado : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainSuperMercadoBinding
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSuperMercadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        val idSuper = sharedPreferences.getString("marketId", "0")

        setSupportActionBar(binding.appbar.toolb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appbar.btnAtras.isVisible = false

        // Configurar ViewPager2
        viewPager = binding.viewPager
        viewPager.adapter = idSuper?.let { ViewPagerAdapter2(this, it) }

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.navigation_AddProduct -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.navigation_Profile -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }

        // Sincronizar ViewPager2 con BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> navView.selectedItemId = R.id.navigation_home
                    1 -> navView.selectedItemId = R.id.navigation_AddProduct
                    2 -> navView.selectedItemId = R.id.navigation_Profile
                }
            }
        })

        // Selecciona el primer ítem al iniciar
        navView.selectedItemId = R.id.navigation_home
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main4, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.AcDe -> {
                // Abrir la actividad 'AcercaDeActivity'
                val intent = Intent(this, AcercaDeActivity::class.java)
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

    private fun cerrarSesion() {
        val editor = sharedPreferences.edit()
        editor.remove("isLoggedIn")
        editor.remove("marketId")
        editor.apply()

        // Redirige al usuario a la pantalla de inicio de sesión
        val intent = Intent(this, ActividadInicioSesion::class.java)
        startActivity(intent)
        finish()
    }
}
