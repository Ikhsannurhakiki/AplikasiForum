package com.ikhsannurhakiki.aplikasiforum.data.source.remote.response

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class ProfileImageResponse(
    @PrimaryKey
    @ColumnInfo(name = "idpengguna")
    @field:SerializedName("idpengguna")
    var id: String,
    var imageBitmap: Bitmap
)
