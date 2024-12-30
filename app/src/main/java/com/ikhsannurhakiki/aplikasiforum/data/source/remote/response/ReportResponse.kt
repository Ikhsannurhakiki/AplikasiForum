package com.ikhsannurhakiki.aplikasiforum.data.source.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ReportResponse(

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("result")
    val result: List<Report>
):Parcelable

@Parcelize
data class Report(

    @field:SerializedName("idreport")
    val reportId: Int,

    @field:SerializedName("idpelapor")
    val reporterId: String,

    @field:SerializedName("namapelapor")
    val reporterName: String,

    @field:SerializedName("idbermasalah")
    val problemId: String,

    @field:SerializedName("matkulbermasalah")
    val probSubject: String,

    @field:SerializedName("catatan")
    val note: String,

    @field:SerializedName("idterlapor")
    val reportedId: String,

    @field:SerializedName("namaterlapor")
    val reportedName: String,

    @field:SerializedName("jenis")
    val reportType: String,

    @field:SerializedName("status")
    var status: String

): Parcelable

@Parcelize
data class PunishmentResponse(
    @field:SerializedName("idpelanggaran")
    val punishId: Int,

    @field:SerializedName("tanggalbatashukuman")
    val date: String,

    @field:SerializedName("jenishukuman")
    val type: String,

    @field:SerializedName("alasanhukuman")
    val note: String
):Parcelable


