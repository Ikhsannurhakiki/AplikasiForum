package com.ikhsannurhakiki.aplikasiforum.interfaces

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionInterface {
     const val CAMERA_PERMISSION_REQUEST_CODE = 101
     private const val CAMERA_REQUIRED_PERMISSIONS = android.Manifest.permission.CAMERA

    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_REQUIRED_PERMISSIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    fun requestCameraPermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

}