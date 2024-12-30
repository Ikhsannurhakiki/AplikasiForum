package com.ikhsannurhakiki.aplikasiforum.data.source.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionResponse(

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("result")
    val result: List<QuestionTagResponse>,

    @field:SerializedName("message")
    val message: String

) : Parcelable

@Parcelize
data class Question(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("idpenanya")
    val askedBy: String,

    @field:SerializedName("judulpertanyaan")
    val title: String,

    @field:SerializedName("pertanyaan")
    val question: String,

    @field:SerializedName("tanggal")
    val date: String,

    @field:SerializedName("tanggaldiedit")
    val editedDate: String,

    @field:SerializedName("jumlahnilai")
    var totalPoint: Int,

    @field:SerializedName("totaljawaban")
    var totalAnswer: Int,

    @field:SerializedName("dilihat")
    var views: Int,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("status")
    val status: String,

    val tag: List<TagResponse>,

    @field:SerializedName("idpengampu")
    val suppLecturer: Int,

    ) : Parcelable

data class AnswerResponse(

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("result")
    val result: List<Answer>
)

@Parcelize
data class Answer(

    @field:SerializedName("idpertanyaan")
    val questionId: Int,

    @field:SerializedName("idjawaban")
    val answerId: Int,

//    @field:SerializedName("score")
//    val score: Int,

    @field:SerializedName("idpenjawab")
    val answerById: String,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("jawaban")
    val answer: String,

    @field:SerializedName("tanggal")
    val date: String,

    @field:SerializedName("tanggaldiedit")
    val editedDate: String,

    val images: List<ImagesResponse>,

    @field:SerializedName("verifikasi")
    val verified: Int,

//    @field:SerializedName("point")
//    val point: Int,

    @field:SerializedName("totalpoint")
    val totalPoint: Int,

    @field:SerializedName("voted")
    val voted: ScoreResponse,

    ) : Parcelable

@Parcelize
data class ScoreResponse(

    @field:SerializedName("votedId")
    val votedId: Int,

    @field:SerializedName("votedById")
    val voteById: String,

    @field:SerializedName("score")
    val score: Int
) : Parcelable

data class AutoIncrementResponse(
    @field:SerializedName("AUTO_INCREMENT")
    val autoIncrement: Int
)

@Parcelize
data class TagResponse(

    @field:SerializedName("id")
    val tagId: Int,

    @field:SerializedName("namatag")
    val tagName: String
) : Parcelable

@Parcelize
data class ImagesResponse(
    @field:SerializedName("namaberkas")
    val images: String,
) : Parcelable

@Parcelize
data class QuestionTagResponse(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("idpenanya")
    val askedBy: String,

    @field:SerializedName("judulpertanyaan")
    val title: String,

    @field:SerializedName("pertanyaan")
    val question: String,

    @field:SerializedName("tanggal")
    val date: String,

    @field:SerializedName("tanggaldiedit")
    val editedDate: String,

    @field:SerializedName("jumlahnilai")
    var totalPoint: Int,

    @field:SerializedName("totaljawaban")
    var totalAnswer: Int,

    @field:SerializedName("dilihat")
    var views: Int,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("idmatkul")
    val subjectId: Int,

    @field:SerializedName("iddosen")
    val lecturerId: String,

    val tag: List<TagResponse>,

    @field:SerializedName("idpengampu")
    val suppLecturer: Int,
) : Parcelable

@Parcelize
data class GeneralResponse(

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("message")
    val message: String
) : Parcelable

@Parcelize
data class QuestionReportedData(

    @field:SerializedName("id")
    val questionId: Int,

    @field:SerializedName("idmatkul")
    val subjectId: Int,

    @field:SerializedName("iddosen")
    val lecturerId: String,

    @field:SerializedName("idpenanya")
    val askedById: String,

    @field:SerializedName("matkul")
    val subject: String,

    @field:SerializedName("status")
    val status: String,
) : Parcelable

@Parcelize
data class Material(

    @field:SerializedName("idmateri")
    val materialId: Int,

    @field:SerializedName("materi")
    val material: String,

    @field:SerializedName("totalselesai")
    val totalFinished: String,

    @field:SerializedName("totalbelumselesai")
    val totalUnfinished: String
) : Parcelable


@Parcelize
data class SuppSubject(

    @field:SerializedName("idmatkul")
    val subjectId: Int,

    @field:SerializedName("ditambahkan")
    val isInsert: Boolean
) : Parcelable
