package com.ikhsannurhakiki.aplikasiforum

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.ApiResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import okhttp3.MultipartBody

interface ForumDataSource {

    //++++++++++++++++++++++++++++QUESTION++++++++++++++++++++++++++
    fun insertQuestion(
        idAskedBy: String,
        title: String,
        question: String,
        subjectId: Int,
        lecturerId: String?,
        listTags: List<String>,
        suppLecturer: Int,
        materialId: Int
    ): LiveData<ApiResponses<String>>

    fun getAllQuestions(
        suppLecturer: Int,
        code: Int,
        key: String,
        materialId: Int
    ): LiveData<PagingData<QuestionTagResponse>>

    fun getDetailQuestion(questionId: Int): LiveData<ApiResponses<QuestionTagResponse>>
    fun updateQuestionViews(questionId: Int, views: Int)
    fun getAllMyQuestion(userId: String): LiveData<ApiResponses<List<QuestionTagResponse>>>
    fun finishQuestion(questionId: Int): LiveData<ApiResponses<Int>>

    //++++++++++++++++++++++++++++ANSWER++++++++++++++++++++++++++
    fun getAnswers(questionId: Int, userId: String): LiveData<ApiResponses<List<Answer>>>
    fun insertAnswer(answer: String, uid: String?, questionId: Int): LiveData<ApiResponses<String>>
    fun deleteQAHandler(deletingId: Int, isQuestion: Boolean): LiveData<ApiResponses<String>>

    //+++++++++++++++++++++++++++SCORE+++++++++++++++++++++++++++++
    fun checkVotedOrNot(questionOId: Int, userId: String?): LiveData<ApiResponses<ScoreResponse>>
    fun checkPoint(questionId: Int): LiveData<ApiResponses<Int>>
    fun votingQuestion(scoreResponse: ScoreResponse, voting: String)


    //+++++++++++++++++++++++++++USER+++++++++++++++++++++++++++++++
    fun insertStudent(user: Student)
    fun getDetailStudent(userId: String): LiveData<ApiResponses<Student>>
    fun getDetailLecturer(userId: String): LiveData<ApiResponses<Lecturer>>
    fun updateDetailUser(user: EditUser)


    //+++++++++++++++++++++++++++SUBJECT+++++++++++++++++++++++++++++++
    fun getSubject(): LiveData<ApiResponses<List<Subject>>>

    //+++++++++++++++++++++++++++LECTURER+++++++++++++++++++++++++++++++
    fun insertLecturer(lecturer: Lecturer)
    fun getLecturerBySubject(subjectId: Int): LiveData<ApiResponses<List<Lecturer>>>
    fun insertLecturerSubject(subjectId: Int, lecturerId: String): LiveData<ApiResponses<String>>
    fun getLecturerSubject(lecturerId: String): LiveData<ApiResponses<List<Subject>>>
    fun getSuppLecturerId(lecturerId: String, subjectId: Int): LiveData<ApiResponses<Int>>

    //+++++++++++++++++++++++++++AUTOINCREMENT+++++++++++++++++++++++++++++++
    fun getCurrentAutoIncrement(): LiveData<ApiResponses<Int>>

    //+++++++++++++++++++++++++++TAGS+++++++++++++++++++++++++++++++
    fun checkTags(tag: String): LiveData<ApiResponses<String>>
    fun insertTags(tag: String)
    fun insertQuestionTags(questionId: String, tag: String)
    fun getTagByQuestion(questionId: Int): LiveData<ApiResponses<List<TagResponse>>>

    //+++++++++++++++++++++++++++TOKEN+++++++++++++++++++++++++++++++
    fun updateUserDeviceToken(uid: String, token: String?)
    fun getToken(userId: String?): LiveData<ApiResponses<String>>
    fun checkAccessRight(userId: String): LiveData<ApiResponses<String>>

    //+++++++++++++++++++++++++++IMAGE+++++++++++++++++++++++++++++++
    fun uploadImages(
        images: MultipartBody.Part,
        lastQuestionId: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>>

    fun getQuestionImage(questionId: Int): LiveData<ApiResponses<List<ImagesResponse>>>

    //+++++++++++++++++++++++++++BOOKMARK+++++++++++++++++++++++++++++++
    fun insertBookmark(
        questionId: Int,
        userId: String,
        status: String
    ): LiveData<ApiResponses<String>>

    fun checkBookmark(questionId: Int, userId: String): LiveData<ApiResponses<String>>
    fun verifiedAnswerHandler(answerId: Int, isVerified: Int): LiveData<ApiResponses<String>>

    //+++++++++++++++++++++++++++REPORT+++++++++++++++++++++++++++++++
    fun insertReport(
        message: String,
        objectReported: Int,
        reporter: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>>

    fun getAllReportedList(userId: String, isQuestion: String): LiveData<ApiResponses<List<Report>>>
    fun insertPunishment(
        reportId: Int,
        type: String,
        formattedDate: String,
        note: String,
        reportType: String
    ): LiveData<ApiResponses<String>>

    fun checkIsAnswerVoted(answerId: Int, userId: String): LiveData<ApiResponses<ScoreResponse>>

    fun checkPunishment(suppLecturer: Int, userId: String): LiveData<ApiResponses<List<PunishmentResponse>>>
    fun answerVotingHandler(userId: String, score: Int,  answerId: Int, s: String): LiveData<ApiResponses<String>>
    fun declineReport(reportId: Int, reason: String): LiveData<ApiResponses<String>>
    fun deleteReportDecision(reportId: Int, status: String, note: String): LiveData<ApiResponses<String>>
    fun getReportedData(reportId: Int, type: String): LiveData<ApiResponses<QuestionReportedData>>
}
