package com.ikhsannurhakiki.aplikasiforum.data.source.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "questionsEntities")
data class QuestionEntity(

    @PrimaryKey
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("askedBy")
    val askedBy: String,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("question")
    val question: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("editedDate")
    val editedDate: String,

    @field:SerializedName("views")
    val views: Int,

    @field:SerializedName("firstname")
    val firstname: String,

    @field:SerializedName("lastname")
    val lastname: String,

    @field:SerializedName("status")
    val status: String,

):Parcelable
