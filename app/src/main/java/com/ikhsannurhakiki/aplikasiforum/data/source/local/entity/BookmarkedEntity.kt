package com.ikhsannurhakiki.aplikasiforum.data.source.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "bookmarkedId")
data class BookmarkedEntity(
    @PrimaryKey
    @field:SerializedName("id")
    val id: Int
): Parcelable