package com.example.stocker2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PREFERENCIAS : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.PreferenceTheme) // Apply the custom theme
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PrefConfig())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
