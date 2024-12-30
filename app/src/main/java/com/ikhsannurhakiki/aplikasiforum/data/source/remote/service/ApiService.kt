package com.ikhsannurhakiki.aplikasiforum.data.source.remote.service

import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    //++++++++++++++++++++++QUESTION++++++++++++++++++
    @FormUrlEncoded
    @POST("questions.php")
    fun insertQuestion(
        @Query("f") f: String,
        @Field("idAskedBy") idAskedBy: String,
        @Field("title") title: String,
        @Field("question") question: String,
        @Field("subjectId") subjectId: Int,
        @Field("lecturerId") lecturerId: String?,
        @Field("tags[]") tags: List<String>?,
        @Field("suppLecturer") suppLecturer: Int,
        @Field("materialId") materialId: Int
    ): Call<String>

    @GET("questions.php")
    suspend fun getAllQuestion(
        @Query("f") f: String,
        @Query("suppLecturer") suppLecturer: Int?,
        @Query("code") code: Int?,
        @Query("key") key: String?,
        @Query("materialId") materialId: Int?,
        @Query("page") page: Int,
        @Query("row_per_page") rowPerPage: Int,
    ): Response<QuestionResponse>

    @GET("questions.php")
    fun getDetailQuestion(
        @Query("f") f: String,
        @Query("questionId") questionId: Int,
    ): Call<QuestionTagResponse>

    @POST("questions.php")
    fun updateQuestionViews(
        @Query("f") f: String,
        @Query("questionId") questionId: Int,
        @Query("views") views: Int
    ): Call<Void>

    @GET("questions.php")
    fun getAllMyQuestion(
        @Query("f") f: String,
        @Query("userId") userid: String,
    ): Call<QuestionResponse>

    //++++++++++++++++++++++Answers++++++++++++++++++
    @GET("questions.php")
    fun getAnswers(
        @Query("f") f: String,
        @Query("questionId") questionId: Int,
        @Query("userId") userId: String,
    ): Call<AnswerResponse>

    @DELETE("questions.php")
    fun deleteQAHandler(
        @Query("f") f: String,
        @Query("deletingId") deletingId: Int,
        @Query("isQuestion") isQuestion: Boolean,
    ): Call<String>

    //+++++++++++++++++++++++SCORE+++++++++++++++++++++
    @FormUrlEncoded
    @POST("questions.php")
    fun insertVoting(
        @Query("f") f: String,
        @Query("voting") voting: String,
        @Field("votedId") votedId: Int,
        @Field("votedById") votedById: String,
        @Field("score") score: Int,
    ): Call<ScoreResponse>

    @DELETE("questions.php")
    fun deleteVoting(
        @Query("f") f: String,
        @Query("voting") voting: String,
        @Query("votedId") votedId: Int,
        @Query("votedById") votedById: String,
        @Query("score") score: Int,
    ): Call<ScoreResponse>

    @GET("questions.php")
    fun checkVotedOrNot(
        @Query("f") f: String,
        @Query("questionId") questionId: Int,
        @Query("userId") userid: String?,
    ): Call<ScoreResponse>

    @GET("questions.php")
    fun checkPoint(
        @Query("f") f: String,
        @Query("questionId") questionId: Int
    ): Call<Int>

    //+++++++++++++++++++++++USER+++++++++++++++++++++
    @FormUrlEncoded
    @POST("user.php")
    fun insertStudent(
        @Query("f") f: String,
        @Field("userid") userid: String,
        @Field("name") name: String,
        @Field("useremail") userEmail: String,
        @Field("accessRights") accessRights: String,
        @Field("npm") npm: String,
        @Field("devicetoken") devicetoken: String
    ): Call<Student>

    @GET("user.php")
    fun getDetailStudent(
        @Query("f") f: String,
        @Query("userId") userId: String,
    ): Call<Student>

    @GET("user.php")
    fun getDetailLecturer(
        @Query("f") f: String,
        @Query("userId") userId: String,
    ): Call<Lecturer>

    @FormUrlEncoded
    @POST("user.php")
    fun editDetailUser(
        @Query("f") f: String,
        @Field("userid") userid: String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("info") info: String
    ): Call<EditUser>

    //+++++++++++++++++++++++SUBJECT+++++++++++++++++++++

    @GET("questions.php")
    fun getSubject(
        @Query("f") f: String,
    ): Call<List<Subject>>

    @GET("questions.php")
    fun getLecturerSubject(
        @Query("f") f: String,
        @Query("lecturerId") lecturerId: String
    ): Call<List<Subject>>

    //+++++++++++++++++++++++LECTURER+++++++++++++++++++++

    @FormUrlEncoded
    @POST("user.php")
    fun insertLecturer(
        @Query("f") f: String,
        @Field("userid") lecturerId: String,
        @Field("name") name: String,
        @Field("useremail") email: String,
        @Field("info") info: String,
        @Field("accessRights") accessRights: String,
        @Field("nip") nip: String,
        @Field("devicetoken") devicetoken: String
    ): Call<Lecturer>

    @GET("questions.php")
    fun getLecturerBySubject(
        @Query("f") f: String,
        @Query("subjectId") subjectId: Int
    ): Call<List<Lecturer>>

    @GET("user.php")
    fun insertLecturerSubject(
        @Query("f") f: String,
        @Query("subjectId") subjectId: Int,
        @Query("lecturerId") lecturerId: String
    ): Call<Unit>

    @FormUrlEncoded
    @POST("user.php")
    fun updateUserDeviceToken(
        @Query("f") f: String,
        @Field("userId") userId: String,
        @Field("token") token: String
    ): Call<Unit>

    @GET("questions.php")
    fun getCurrentAutoIncrement(
        @Query("f") f: String,
    ): Call<Int>

    @GET("questions.php")
    fun checkTags(
        @Query("f") f: String,
        @Query("tag") tag: String
    ): Call<String>

    @GET("questions.php")
    fun insertTag(
        @Query("f") f: String,
        @Query("tag") tag: String
    ): Call<Unit>

    @GET("questions.php")
    fun insertQuestionTag(
        @Query("f") f: String,
        @Query("questionId") questionId: String,
        @Query("tagId") tagId: String
    ): Call<Unit>

    @GET("questions.php")
    fun getTagbyQuestion(
        @Query("f") f: String,
        @Query("questionId") questionId: Int
    ): Call<List<TagResponse>>

    @GET("questions.php")
    fun finishQuestion(
        @Query("f") f: String,
        @Query("questionId") questionId: Int
    ): Call<Unit>

    @GET("questions.php")
    fun getToken(
        @Query("f") f: String,
        @Query("userId") userId: String?
    ): Call<String>

    @Multipart
    @POST("upload.php")
    fun uploadImages(
        @Query("f") f: String,
        @Part images: MultipartBody.Part,
        @Query("lastQuestionId") lastQuestionId: String,
        @Query("isQuestion") isQuestion: String
    ): Call<String>

    @GET("user.php")
    fun checkAccess(
        @Query("f") f: String,
        @Query("userid") userId: String?
    ): Call<String>

    @GET("questions.php")
    fun getQuestionsImage(
        @Query("f") f: String,
        @Query("questionId") questionId: Int
    ): Call<List<ImagesResponse>>

    @FormUrlEncoded
    @POST("questions.php")
    fun insertAnswer(
        @Query("f") f: String,
        @Field("answer") answer: String,
        @Field("uid") uid: String?,
        @Field("questionId") questionId: Int
    ): Call<GeneralResponse>

    @FormUrlEncoded
    @POST("questions.php")
    fun insertBookmark(
        @Query("f") f: String,
        @Field("uid") uid: String?,
        @Field("questionId") questionId: Int,
        @Field("status") status: String
    ): Call<String>

    @GET("questions.php")
    fun checkBookmark(
        @Query("f") f: String,
        @Query("uid") uid: String?,
        @Query("questionId") questionId: Int
    ): Call<String>

    @FormUrlEncoded
    @POST("questions.php")
    fun verifiedAnswerHandler(
        @Query("f") f: String,
        @Field("answerId") answerId: Int,
        @Field("isVerified") isVerified: Int,
    ): Call<String>

    @FormUrlEncoded
    @POST("questions.php")
    fun reportHandler(
        @Query("f") f: String,
        @Field("message") message: String,
        @Field("reporter") reporter: String,
        @Field("objectReported") objectReported: Int,
        @Field("isQuestion") isQuestion: String
    ): Call<String>

    @GET("questions.php")
    fun getAllReportedList(
        @Query("f") f: String,
        @Query("userId") userId: String,
        @Query("isQuestion") isQuestion: String
    ): Call<List<Report>>

    @GET("questions.php")
    fun getSuppLecturerId(
        @Query("f") s: String,
        @Query("lecturerId") lecturerId: String,
        @Query("subjectId") subjectId: Int
    ): Call<Int>

    @FormUrlEncoded
    @POST("questions.php")
    fun insertPunishment(
        @Query("f") s: String,
        @Field("reportId") reportId: Int,
        @Field("type") type: String,
        @Field("formattedDate") formattedDate: String,
        @Field("note") note: String,
        @Field("reportType") reportType: String
    ): Call<String>

    @GET("questions.php")
    fun checkPunishment(
        @Query("f") s: String,
        @Query("suppLecturer") suppLecturer: Int,
        @Query("userId") userId: String
    ): Call<List<PunishmentResponse>>

    @GET("questions.php")
    fun checkIsAnswerVoted(
        @Query("f") s: String,
        @Query("answerId") answerId: Int,
        @Query("userId") userId: String
    ): Call<ScoreResponse>

    @FormUrlEncoded
    @POST("questions.php")
    fun answerVotingHandler(
        @Query("f") s: String,
        @Field("userId") userId: String,
        @Field("score") score: Int,
        @Field("answerId") answerId: Int,
        @Field("fun") f: String
    ): Call<String>

    @FormUrlEncoded
    @POST("questions.php")
    fun declineReport(
        @Query("f") s: String,
        @Field("reportId") reportId: Int,
        @Field("reason") reason: String,
    ): Call<String>


    @GET("questions.php")
    fun deleteReportDecision(
        @Query("f") s: String,
        @Query("reportId") reportId: Int,
        @Query("status") status: String,
        @Query("note") note: String
    ): Call<String>

    @GET("questions.php")
    fun getReportedData(
        @Query("f") s: String,
        @Query("reportId") reportId: Int,
        @Query("type") type: String
    ): Call< QuestionReportedData>

    @GET("questions.php")
    fun getAllMaterial(
        @Query("f") s: String,
        @Query("subjectId") subjectId: Int,
        @Query("suppLecturer") suppLecturer: Int
    ): Call<List<Material>>

    @GET("questions.php")
    fun addSubject(
        @Query("f") s: String,
        @Query("subjectName")subjectName: String,
        @Query("sks")sks: String
    ): Call<String>

    @GET("questions.php")
    fun editSubject(
        @Query("f") s: String,
        @Query("subjectId")subjectId: Int,
        @Query("subjectName") subjectName: String,
        @Query("sks")sks: String
    ): Call<String>

    @GET("questions.php")
    fun deleteSubject(
        @Query("f") s: String,
        @Query("subjectId")subjectId: Int,
    ): Call<String>

    @GET("questions.php")
    fun addMaterial(
        @Query("f") s: String,
        @Query("subjectId")subjectId: Int,
        @Query("materialName") materialName: String
    ): Call<String>

    @GET("questions.php")
    fun editMaterial(
        @Query("f") s: String,
        @Query("materialId")materialId: Int,
        @Query("materialName") materialName: String,
    ): Call<String>

    @GET("questions.php")
    fun deleteMaterial(
        @Query("f") s: String,
        @Query("materialId") materialId: Int
    ): Call<String>

    @GET("questions.php")
    fun handleSaveSubjectLiveData(
        @Query("f") s: String,
        @Query("userId") userId: String,
        @Query("subjectId")subjectId: Int,
        @Query("isInsert")isInsert: String
    ): Call<String>
}