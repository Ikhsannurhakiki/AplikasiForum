package com.ikhsannurhakiki.aplikasiforum.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityMain2Binding
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel

val Context.dataStore by preferencesDataStore("auth")
lateinit var viewModel: HomeViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private var actionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = UserPreferences.getInstance(dataStore)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar = supportActionBar
        actionBar?.let {
            it.title = resources.getString(R.string.app_name)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        navHostFragment?.navController?.let {
            NavigationUI.setupWithNavController(
                binding.bttmNav,
                it
            )
        }
        supportActionBar?.elevation = 0f

        val context: Context = applicationContext // replace with your application context
        val notificationManager = NotificationManagerCompat.from(context)

        if (!notificationManager.areNotificationsEnabled()) {
            // Request notification permissions
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
            }
            startActivity(intent)
        }
    }



    fun setActionBarTitle(title: String?) {
        actionBar?.title = title
    }
}