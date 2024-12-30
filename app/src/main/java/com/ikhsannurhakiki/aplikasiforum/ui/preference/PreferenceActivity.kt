package com.ikhsannurhakiki.aplikasiforum.ui.preference

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ikhsannurhakiki.aplikasiforum.R

class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        supportFragmentManager.beginTransaction().add(R.id.setting_holder, PreferenceFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}