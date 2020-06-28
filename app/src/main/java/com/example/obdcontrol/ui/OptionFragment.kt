package com.example.obdcontrol.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.obdcontrol.R

class OptionFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.default_preference, rootKey)
    }
}