package com.example.stocker2

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.stocker2.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferencias, rootKey)
    }
}
