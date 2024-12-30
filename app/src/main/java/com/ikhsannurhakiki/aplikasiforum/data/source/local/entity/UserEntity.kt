package com.ikhsannurhakiki.aplikasiforum.data.source.local.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "studentEntities")
data class StudentEntity(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "idpengguna")
    @field:SerializedName("idpengguna")
    var id: String,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("info")
    val info: String,

    @field:SerializedName("hakakses")
    val accessRights: String,

    @field:SerializedName("devicetoken")
    val deviceToken: String,

    @field:SerializedName("npm")
    val npm: String,

    @field:SerializedName("angkatan")
    val year: String

)

