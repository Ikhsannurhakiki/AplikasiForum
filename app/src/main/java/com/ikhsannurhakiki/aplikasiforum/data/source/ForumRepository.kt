package com.ikhsannurhakiki.aplikasiforum.data.source

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.ikhsannurhakiki.aplikasiforum.ForumDataSource
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.ApiResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.RemoteDataSource
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import com.ikhsannurhakiki.aplikasiforum.utils.AppExecutors
import okhttp3.MultipartBody

class ForumRepository private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val appExecutors: AppExecutors
) : ForumDataSource {

    companion object {
        @Volatile
        private var instance: ForumRepository? = null

        fun getInstance(
            remoteDataSource: RemoteDataSource,
            appExecutors: AppExecutors
        ): ForumRepository =
            instance ?: synchronized(this) {
                instance ?: ForumRepository(
                    remoteDataSource,
                    appExecutors
                ).apply { instance = this }
            }
    }

    //+++++++++++++++++++++++++++++++QUESTION++++++++++++++++ +++++++++++
    override fun insertQuestion(
        idAskedBy: String,
        title: String,
        question: String,
        subjectId: Int,
        lecturerId: String?,
        listTags: List<String>,
        suppLecturer: Int,
        materialId: Int
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.insertQuestion(
            idAskedBy,
            title,
            question,
            subjectId,
            lecturerId,
            listTags,
            suppLecturer,
            materialId
        )


    override fun getAllQuestions(
        suppLecturer: Int,
        code: Int,
        key: String,
        materialId: Int
    ): LiveData<PagingData<QuestionTagResponse>> =
        remoteDataSource.getAllQuestion(suppLecturer, code, key, materialId)


    override fun getDetailQuestion(questionId: Int): LiveData<ApiResponses<QuestionTagResponse>> =
        remoteDataSource.getDetailQuestion(questionId)

    override fun updateQuestionViews(questionId: Int, views: Int) =
        remoteDataSource.updateQuestionViews(questionId, views)

    override fun getAllMyQuestion(userId: String): LiveData<ApiResponses<List<QuestionTagResponse>>> =
        remoteDataSource.getAllMyQuestion(userId)

    override fun finishQuestion(questionId: Int): LiveData<ApiResponses<Int>> =
        remoteDataSource.finishQuestion(questionId)

    override fun deleteQAHandler(
        deletingId: Int,
        isQuestion: Boolean
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.deleteQAHandler(deletingId, isQuestion)

    //+++++++++++++++++++++++++++++++ANSWER+++++++++++++++++++++++++++
    override fun getAnswers(questionId: Int, userId: String): LiveData<ApiResponses<List<Answer>>> =
        remoteDataSource.getAnswers(questionId, userId)

    //++++++++++++++++++++++++++++++SCORE++++++++++++++++++++++++++++++++
    override fun votingQuestion(scoreResponse: ScoreResponse, voting: String) =
        appExecutors.diskIO().execute { remoteDataSource.votingQuestion(scoreResponse, voting) }

    override fun checkVotedOrNot(
        questionId: Int,
        userId: String?
    ): LiveData<ApiResponses<ScoreResponse>> =
        remoteDataSource.checkVotedOrNot(questionId, userId)

    override fun checkPoint(questionId: Int): LiveData<ApiResponses<Int>> =
        remoteDataSource.checkPoint(questionId)

    //++++++++++++++++++++++++++++++USER+++++++++++++++++++++++++++++++++
    override fun insertStudent(user: Student) =
        appExecutors.diskIO().execute { remoteDataSource.insertStudent(user) }

    override fun getDetailLecturer(userId: String): LiveData<ApiResponses<Lecturer>> =
        remoteDataSource.getDetailLecturer(userId)


    override fun getDetailStudent(userId: String): LiveData<ApiResponses<Student>> =
        remoteDataSource.getDetailStudent(userId)

    override fun updateDetailUser(user: EditUser) =
        appExecutors.diskIO().execute { remoteDataSource.updateDetailUser(user) }

    //++++++++++++++++++++++++++++++SUBJECT+++++++++++++++++++++++++++++++++
    override fun getSubject(): LiveData<ApiResponses<List<Subject>>> =
        remoteDataSource.getSubject()

    override fun getLecturerSubject(lecturerId: String): LiveData<ApiResponses<List<Subject>>> =
        remoteDataSource.getLecturerSubject(lecturerId)

    //++++++++++++++++++++++++++++++LECTURER+++++++++++++++++++++++++++++++++
    override fun insertLecturer(lecturer: Lecturer) =
        appExecutors.diskIO().execute { remoteDataSource.insertLecturer(lecturer) }

    override fun getLecturerBySubject(subjectId: Int): LiveData<ApiResponses<List<Lecturer>>> =
        remoteDataSource.getLecturerBySubject(subjectId)

    override fun insertLecturerSubject(subjectId: Int, lecturerId: String): LiveData<ApiResponses<String>> =
        remoteDataSource.insertLecturerSubject(subjectId, lecturerId)

    //++++++++++++++++++++++++++++++TOKEN+++++++++++++++++++++++++++++++++
    override fun updateUserDeviceToken(uid: String, token: String?) =
        remoteDataSource.updateUserDeviceToken(uid, token)

    override fun getToken(userId: String?): LiveData<ApiResponses<String>> =
        remoteDataSource.getToken(userId)

    override fun checkAccessRight(userId: String): LiveData<ApiResponses<String>> =
        remoteDataSource.checkAccessRight(userId)

    //++++++++++++++++++++++++++++++AUTOINCREMENT+++++++++++++++++++++++++++++++++
    override fun getCurrentAutoIncrement() = remoteDataSource.getCurrentAutoIncrement()

    //++++++++++++++++++++++++++++++TAGS+++++++++++++++++++++++++++++++++
    override fun checkTags(tag: String) = remoteDataSource.checkTags(tag)
    override fun insertTags(tag: String) = remoteDataSource.insertTags(tag)
    override fun insertQuestionTags(questionId: String, tagId: String) =
        remoteDataSource.insertQuestionTags(questionId, tagId)

    override fun getTagByQuestion(questionId: Int): LiveData<ApiResponses<List<TagResponse>>> =
        remoteDataSource.getTagByQuestion(questionId)

    override fun uploadImages(
        images: MultipartBody.Part,
        lastQuestionId: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.uploadImages(images, lastQuestionId, isQuestion)

    override fun getQuestionImage(questionId: Int): LiveData<ApiResponses<List<ImagesResponse>>> =
        remoteDataSource.getQuestionsImage(questionId)

    override fun insertAnswer(
        answer: String,
        uid: String?,
        questionId: Int
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.insertAnswer(answer, uid, questionId)

    override fun insertBookmark(
        questionId: Int,
        userId: String,
        status: String
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.insertBookmark(questionId, userId, status)

    override fun checkBookmark(questionId: Int, userId: String): LiveData<ApiResponses<String>> =
        remoteDataSource.checkBookmark(questionId, userId)

    override fun verifiedAnswerHandler(
        answerId: Int,
        isVerified: Int
    ): LiveData<ApiResponses<String>> = remoteDataSource.verifiedAnswerHandler(answerId, isVerified)

    override fun insertReport(
        message: String,
        objectReported: Int,
        reporter: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.insertReport(message, objectReported, reporter, isQuestion)

    override fun getAllReportedList(userId: String, isQuestion: String): LiveData<ApiResponses<List<Report>>> =
        remoteDataSource.getAllReportedList(userId, isQuestion)

    override fun getSuppLecturerId(
        lecturerId: String,
        subjectId: Int
    ): LiveData<ApiResponses<Int>> =
        remoteDataSource.getSuppLecturerId(lecturerId, subjectId)

    override fun insertPunishment(
        reportid: Int,
        type: String,
        formattedDate: String,
        note: String,
        reportType: String
    ): LiveData<ApiResponses<String>> =
        remoteDataSource.insertPunishment(reportid, type, formattedDate, note, reportType)

    override fun checkPunishment(
        suppLecturer: Int,
        userId: String
    ): LiveData<ApiResponses<List<PunishmentResponse>>> =
        remoteDataSource.checkPunishment(suppLecturer, userId)

    override fun answerVotingHandler(userId:String, score:Int, answerId: Int, s: String): LiveData<ApiResponses<String>> =
        remoteDataSource.answerVotingHandler(userId, score, answerId, s)

    override fun checkIsAnswerVoted(
        answerId: Int,
        userId: String
    ): LiveData<ApiResponses<ScoreResponse>> = remoteDataSource.checkIsAnswerVoted(answerId, userId)

    override fun declineReport(reportId : Int, reason: String): LiveData<ApiResponses<String>> =
        remoteDataSource.declineReport(reportId, reason)

    override fun deleteReportDecision(reportId: Int, status: String, note: String): LiveData<ApiResponses<String>> =
        remoteDataSource.deleteReportDecision(reportId, status, note)

    override fun getReportedData(reportId: Int, type: String): LiveData<ApiResponses<QuestionReportedData>> =
        remoteDataSource.getReportedData(reportId, type)
}