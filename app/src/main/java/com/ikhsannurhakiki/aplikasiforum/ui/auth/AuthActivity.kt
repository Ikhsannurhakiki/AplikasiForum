package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityAuthBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel

val Context.dataStore by preferencesDataStore("auth")
lateinit var viewModel: HomeViewModel

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras

        if (bundle?.get("qustionId") == 32) {
            Log.d("questionId", bundle.get("qustionId").toString())
        }else{
            val mFragmentManager = supportFragmentManager
            val mLoginFragment = LoginFragment()
            val fragment = mFragmentManager.findFragmentByTag(LoginFragment::class.java.simpleName)
            if (fragment !is LoginFragment) {
                mFragmentManager
                    .beginTransaction()
                    .add(R.id.frame_container, mLoginFragment, LoginFragment::class.java.simpleName)
                    .commit()
            }
        }

        window.statusBarColor = ContextCompat.getColor(this,R.color.green_500) //status bar or the time bar at the top (see example image1)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.green_500) // Navigation bar the soft bottom of some phones like nexus and some Samsung note series  (see example image2)
    }


}