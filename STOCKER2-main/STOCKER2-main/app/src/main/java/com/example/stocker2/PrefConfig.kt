package com.example.stocker2

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class PrefConfig : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferencias, rootKey)
    }
}
