package com.ikhsannurhakiki.aplikasiforum.ui.preference

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.core.os.ConfigurationCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ikhsannurhakiki.aplikasiforum.R

class PreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var language: String
    private lateinit var isLanguageSet: Preference
    private lateinit var currentlocale: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        currentlocale = ConfigurationCompat.getLocales(resources.configuration)[0].toString()
        init()
        setSummaries()
    }

    private fun init(){
        language = resources.getString(R.string.language_key)
        isLanguageSet = findPreference<Preference>(language) as Preference
    }

    private fun setSummaries() {
        val sh = preferenceManager.sharedPreferences
        isLanguageSet.summary = sh?.getString(language, "Indonesia")

        if(currentlocale == "en_US"){
            isLanguageSet.summary = "English"
        }else{
            isLanguageSet.summary = "Indonesia"
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key = preference.key
        if(key == language){
            isLanguageSet.intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        TODO("Not yet implemented")
    }
}