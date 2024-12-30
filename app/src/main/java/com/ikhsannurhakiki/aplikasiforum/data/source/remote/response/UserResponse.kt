package com.ikhsannurhakiki.aplikasiforum.data.source.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//User(Mhs)
@Parcelize
data class Student(
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
    var deviceToken: String,

    @field:SerializedName("npm")
    val npm: String,

    @field:SerializedName("angkatan")
    val year: String

) : Parcelable

@Parcelize
data class Lecturer(
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
    var deviceToken: String,

    @field:SerializedName("nip")
    val nip: String,

    @field:SerializedName("idpengampu")
    val suppLecturer: Int

) : Parcelable

@Parcelize
data class EditUser(
    @field:SerializedName("idpengguna")
    var id: String,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("info")
    val info: String,
) : Parcelable

//Matkul
@Parcelize
data class Subject(
    @field:SerializedName("id")
    var id: Int,

    @field:SerializedName("matkul")
    val subject: String,

    @field:SerializedName("SKS")
    val sks: String,
) : Parcelable

data class Auth(
    var userId: String?,
    var name: String?,
    var accessRights: String,
    var isLoggedIn: Boolean
)

