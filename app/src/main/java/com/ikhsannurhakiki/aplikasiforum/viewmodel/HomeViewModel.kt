package com.ikhsannurhakiki.aplikasiforum.viewmodel

import PagingDataSource
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.data.source.ForumRepository
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.ApiResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.service.ApiConfig2
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class HomeViewModel(private val forumRepository: ForumRepository) : ViewModel() {


    var answer: String? = null
    val getFile = ArrayList<ImageFileList>()

    //++++++++++++++++++++++++++++++++++++QUESTION+++++++++++++++++++++++++++
    //INSERT
    fun insertQuestion(
        idAskedBy: String,
        title: String,
        question: String,
        subjectId: Int,
        lecturerId: String?,
        listTags: List<String>,
        suppLecturer: Int,
        materialId: Int
    ): LiveData<ApiResponses<String>> =
        forumRepository.insertQuestion(
            idAskedBy,
            title,
            question,
            subjectId,
            lecturerId,
            listTags,
            suppLecturer,
            materialId
        )

    //GET

//    fun getAllQuestions(
//        lecturerId: String?,
//        subjectId: Int,
//        code: Int,
//        key: String
//    ): LiveData<PagingData<QuestionTagResponse>> {
//        return forumRepository.getAllQuestions(lecturerId, subjectId, code, key)
//            .cachedIn(viewModelScope)
//    }


    fun getQuestionsImage(questionId: Int): LiveData<ApiResponses<List<ImagesResponse>>> =
        forumRepository.getQuestionImage(questionId)

    private val _allQuestionLiveData = MutableLiveData<PagingData<QuestionTagResponse>>()
    val allQuestionLiveData: LiveData<PagingData<QuestionTagResponse>> get() = _allQuestionLiveData

    fun getAllQuestions(
        suppLecturer: Int,
        code: Int?,
        key: String?,
        materialId: Int
    ) {
        val pagingFlow = Pager(
            config = PagingConfig(
                pageSize = 1,
                enablePlaceholders = false,
                initialLoadSize = 2
            ),
            pagingSourceFactory = {
                PagingDataSource(ApiConfig2.getApiService(), suppLecturer, code, key, materialId)
            }, initialKey = 1
        ).flow.cachedIn(viewModelScope)
        pagingFlow.asLiveData().observeForever {
            _allQuestionLiveData.value = it
        }
    }

    fun getDetailQuestion(questionId: Int): LiveData<ApiResponses<QuestionTagResponse>> =
        forumRepository.getDetailQuestion(questionId)

    fun getAllMyQuestion(userId: String): LiveData<ApiResponses<List<QuestionTagResponse>>> =
        forumRepository.getAllMyQuestion(userId)

    //UPDATE
    fun updateQuestionViews(questionId: Int, views: Int) =
        forumRepository.updateQuestionViews(questionId, views)

    fun finishQuestion(questionId: Int): LiveData<ApiResponses<Int>> =
        forumRepository.finishQuestion(questionId)

    fun deleteQAHandler(deletingId: Int, isQuestion: Boolean): LiveData<ApiResponses<String>> =
        forumRepository.deleteQAHandler(deletingId, isQuestion)

    //++++++++++++++++++++++++++++++++++++ANSWER+++++++++++++++++++++++++++
    //GET
    fun getAnswer(questionId: Int, userId: String): LiveData<ApiResponses<List<Answer>>> =
        forumRepository.getAnswers(questionId, userId)

    //++++++++++++++++++++++++++++++++++++SCORE++++++++++++++++++++++++++++++
    fun votingQuestion(scoreResponse: ScoreResponse, voting: String) =
        forumRepository.votingQuestion(scoreResponse, voting)

    fun checkPoint(questionId: Int): LiveData<ApiResponses<Int>> =
        forumRepository.checkPoint(questionId)

    fun checkVotedOrNot(id: Int, userId: String?): LiveData<ApiResponses<ScoreResponse>> =
        forumRepository.checkVotedOrNot(id, userId)

    //++++++++++++++++++++++++++++++++++++User+++++++++++++++++++++++++++++++
    //INSERT
    fun insertLecturer(user: Lecturer) = forumRepository.insertLecturer(user)
    fun insertStudent(user: Student) = forumRepository.insertStudent(user)

    //GET
    fun getDetailLecturer(userId: String): LiveData<ApiResponses<Lecturer>> =
        forumRepository.getDetailLecturer(userId)

    fun getDetailStudent(userId: String): LiveData<ApiResponses<Student>> =
        forumRepository.getDetailStudent(userId)

    //UPDATE
    fun updateDetailUser(user: EditUser) = forumRepository.updateDetailUser(user)

    //++++++++++++++++++++++++++++++++++++SUBJECT+++++++++++++++++++++++++++++++
    //INSERT
    fun insertLecturerSubject(subjectId: Int, lecturerId: String) : LiveData<ApiResponses<String>> =
        forumRepository.insertLecturerSubject(subjectId, lecturerId)

    private val _subjectLiveData = MutableLiveData<ApiResponses<List<Subject>>>()
    val subjectLiveData: LiveData<ApiResponses<List<Subject>>> get() = _subjectLiveData
    fun getSubject() {
        ApiConfig2.getApiService().getSubject(
            "getSubject"
        ).enqueue(object : Callback<List<Subject>> {
            override fun onResponse(
                call: Call<List<Subject>>,
                response: Response<List<Subject>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _subjectLiveData.value = ApiResponses.success(it)
                        } else {
                            _subjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _subjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }


    fun getLecturerSubject(lecturerId: String): LiveData<ApiResponses<List<Subject>>> =
        forumRepository.getLecturerSubject(lecturerId)


    //++++++++++++++++++++++++++++++++++++LECTURER+++++++++++++++++++++++++++++++
//    fun insertLecturer(lecturer:Lecturer) = forumRepository.insertLecturer(lecturer)
    //GET
    private val _getLecturerBySubjectLiveData = MutableLiveData<ApiResponses<List<Lecturer>>>()
    val getLecturerBySubjectLiveData: LiveData<ApiResponses<List<Lecturer>>> =
        _getLecturerBySubjectLiveData

    fun getSuppLecturerId(lecturerId: String, subjectId: Int): LiveData<ApiResponses<Int>> =
        forumRepository.getSuppLecturerId(lecturerId, subjectId)

    fun getLecturerBySubject(subjectId: Int) {
        ApiConfig2.getApiService().getLecturerBySubject(
            "getLecturerBySubject",
            subjectId
        ).enqueue(object : Callback<List<Lecturer>> {
            override fun onResponse(
                call: Call<List<Lecturer>>,
                response: Response<List<Lecturer>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _getLecturerBySubjectLiveData.value = ApiResponses.success(it)
                        } else {
                            _getLecturerBySubjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _getLecturerBySubjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Lecturer>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    //++++++++++++++++++++++++++++++++++++TOKEN+++++++++++++++++++++++++++++++
    //GET
    fun getToken(userId: String?): LiveData<ApiResponses<String>> = forumRepository.getToken(userId)

    //UPDATE
    fun updateUserDeviceToken(uid: String, token: String?) =
        forumRepository.updateUserDeviceToken(uid, token)

    //++++++++++++++++++++++++++++++++++++AUTOINCREMENT+++++++++++++++++++++++++++++++
    //GET
    fun checkCurrentAutoIncrement(): LiveData<ApiResponses<Int>> =
        forumRepository.getCurrentAutoIncrement()

    //++++++++++++++++++++++++++++++++++++TAGS+++++++++++++++++++++++++++++++
    //INSERT
    fun insertTag(tag: String) = forumRepository.insertTags(tag)
    fun insertQuestionTag(questionId: String, tagId: String) =
        forumRepository.insertQuestionTags(questionId, tagId)

    //GET
    fun checkTags(tag: String): LiveData<ApiResponses<String>> = forumRepository.checkTags(tag)
    fun getTagByQuestion(questionId: Int): LiveData<ApiResponses<List<TagResponse>>> =
        forumRepository.getTagByQuestion(questionId)

    //++++++++++++++++++++++++++++++++++++IMAGE+++++++++++++++++++++++++++++++

    private val _imageLiveData = MutableLiveData<Bitmap>()
    val imageLiveData: LiveData<Bitmap>
        get() = _imageLiveData

    fun checkProfileImage(imageId: String) {
        val ref = FirebaseStorage.getInstance().reference.child("img/${imageId}")
        val localFile = File.createTempFile(imageId, ".jpeg")
        ref.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                _imageLiveData.value = bitmap
            }
    }

    fun checkAccessRight(userId: String) = forumRepository.checkAccessRight(userId)

    fun uploadImages(
        images: MultipartBody.Part,
        lastQuestionId: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> =
        forumRepository.uploadImages(images, lastQuestionId, isQuestion)

    fun insertAnswer(
        answer: String,
        uid: String?,
        questionId: Int
    ): LiveData<ApiResponses<String>> = forumRepository.insertAnswer(answer, uid, questionId)

    fun insertBookmark(
        questionId: Int,
        userId: String,
        status: String
    ): LiveData<ApiResponses<String>> = forumRepository.insertBookmark(questionId, userId, status)

    fun checkBookmark(questionId: Int, userId: String): LiveData<ApiResponses<String>> =
        forumRepository.checkBookmark(questionId, userId)

    fun verifiedAnswerHandler(answerId: Int, isVerified: Int): LiveData<ApiResponses<String>> =
        forumRepository.verifiedAnswerHandler(answerId, isVerified)

    fun insertReport(
        message: String,
        objectReported: Int,
        reporter: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> =
        forumRepository.insertReport(message, objectReported, reporter, isQuestion)

    ///////////////////////////////////
    private val _data = MutableLiveData<String>()
    val dataQuestionStatus: LiveData<String> get() = _data

    fun updateQuestionStatus(newValue: String) {
        _data.value = newValue
    }

    fun getAllReportedList(userId: String, isQuestion: String): LiveData<ApiResponses<List<Report>>> =
        forumRepository.getAllReportedList(userId, isQuestion)

    fun insertPunishment(
        reportId: Int,
        type: String,
        formattedDate: String,
        note: String,
        reportType: String
    ): LiveData<ApiResponses<String>> =
        forumRepository.insertPunishment(reportId, type, formattedDate, note, reportType)

    fun checkPunishment(
        suppLecturer: Int, userId: String
    ): LiveData<ApiResponses<List<PunishmentResponse>>> = forumRepository.checkPunishment(
        suppLecturer, userId
    )

    fun checkIsAnswerVoted(
        answerId: Int, userId: String
    ): LiveData<ApiResponses<ScoreResponse>> = forumRepository.checkIsAnswerVoted(answerId, userId)

    fun answerVotingHandler(userId: String, score: Int ,answerId:Int, s:String): LiveData<ApiResponses<String>> = forumRepository.answerVotingHandler(userId, score, answerId, s)

    fun declineReport(reportId : Int, reason: String): LiveData<ApiResponses<String>> = forumRepository.declineReport(reportId, reason)
    fun deleteReportDecision(reportId: Int, status: String, note: String): LiveData<ApiResponses<String>> = forumRepository.deleteReportDecision(reportId, status, note)
    fun getReportedData(reportId: Int, type: String): LiveData<ApiResponses<QuestionReportedData>> = forumRepository.getReportedData(reportId, type)

    private val _getMaterialListLiveData = MutableLiveData<ApiResponses<List<Material>>>()
    val getMaterialListLiveData: LiveData<ApiResponses<List<Material>>> =
        _getMaterialListLiveData

    fun getAllMaterial(subjectId: Int, suppLecturer: Int) {
        ApiConfig2.getApiService().getAllMaterial(
            "getAllMaterials",
            subjectId,
            suppLecturer
        ).enqueue(object : Callback<List<Material>> {
            override fun onResponse(
                call: Call<List<Material>>,
                response: Response<List<Material>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _getMaterialListLiveData.value = ApiResponses.success(it)
                        } else {
                            _getMaterialListLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _getMaterialListLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _getMaterialMListLiveData = MutableLiveData<ApiResponses<List<Material>>>()
    val getMaterialMListLiveData: LiveData<ApiResponses<List<Material>>> =
        _getMaterialMListLiveData

    fun getAllMaterialM(subjectId: Int) {
        ApiConfig2.getApiService().getAllMaterial(
            "getAllMaterialsManage",
            subjectId,
            0
        ).enqueue(object : Callback<List<Material>> {
            override fun onResponse(
                call: Call<List<Material>>,
                response: Response<List<Material>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _getMaterialMListLiveData.value = ApiResponses.success(it)
                        } else {
                            _getMaterialMListLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _getMaterialMListLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Material>>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _addSubjectLiveData = MutableLiveData<ApiResponses<String>>()
    val addSubjectLiveData: LiveData<ApiResponses<String>> get() = _addSubjectLiveData
    fun addSubject(subjectName: String, sks: String) {
        ApiConfig2.getApiService().addSubject(
            "addSubject",
            subjectName,
            sks
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _addSubjectLiveData.value = ApiResponses.success(it)
                        } else {
                            _addSubjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _addSubjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _editSubjectLiveData = MutableLiveData<ApiResponses<String>>()
    val editSubjectLiveData: LiveData<ApiResponses<String>> get() = _editSubjectLiveData
    fun editSubject(subjectId: Int, subjectName: String, sks: String) {
        ApiConfig2.getApiService().editSubject(
            "updateSubject",
            subjectId,
            subjectName,
            sks
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _editSubjectLiveData.value = ApiResponses.success(it)
                        } else {
                            _editSubjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _editSubjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _deleteSubjectLiveData = MutableLiveData<ApiResponses<String>>()
    val deleteSubjectLiveData: LiveData<ApiResponses<String>> get() = _deleteSubjectLiveData
    fun deleteSubject(subjectId: Int) {
        ApiConfig2.getApiService().deleteSubject(
            "deleteSubject",
            subjectId
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _deleteSubjectLiveData.value = ApiResponses.success(it)
                        } else {
                            _deleteSubjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _deleteSubjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _addMaterialLiveData = MutableLiveData<ApiResponses<String>>()
    val addMaterialLiveData: LiveData<ApiResponses<String>> get() = _addMaterialLiveData
    fun addMaterial(subjectId:Int, materialName: String) {
        ApiConfig2.getApiService().addMaterial(
            "addMaterial",
            subjectId,
            materialName
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _addMaterialLiveData.value = ApiResponses.success(it)
                        } else {
                            _addMaterialLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _addMaterialLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _editMaterialLiveData = MutableLiveData<ApiResponses<String>>()
    val editMaterialLiveData: LiveData<ApiResponses<String>> get() = _editMaterialLiveData
    fun editMaterial(materialId: Int, materialName: String) {
        ApiConfig2.getApiService().editMaterial(
            "updateMaterial",
            materialId,
            materialName
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _editMaterialLiveData.value = ApiResponses.success(it)
                        } else {
                            _editMaterialLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _editMaterialLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _deleteMaterialLiveData = MutableLiveData<ApiResponses<String>>()
    val deleteMaterialLiveData: LiveData<ApiResponses<String>> get() = _deleteMaterialLiveData
    fun deleteMaterial(materialId: Int) {
        ApiConfig2.getApiService().deleteMaterial(
            "deleteMaterial",
            materialId
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            _deleteMaterialLiveData.value = ApiResponses.success(it)
                        } else {
                            _deleteMaterialLiveData.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    _deleteMaterialLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(ContentValues.TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    private val _handleSaveSubjectLiveData = MutableLiveData<ApiResponses<String>>()
    val handleSaveSubjectLiveData: LiveData<ApiResponses<String>> get() = _handleSaveSubjectLiveData
    fun handleSaveSubjectLiveData(subjectId: Int, isInsert:Boolean, userId: String) {
            ApiConfig2.getApiService().handleSaveSubjectLiveData(
                "handleSaveSubjectLiveData",
                userId,
                subjectId,
                isInsert.toString()
            ).enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                _handleSaveSubjectLiveData.value = ApiResponses.success(it)
                            } else {
                                _handleSaveSubjectLiveData.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }
                    } else {
                        _handleSaveSubjectLiveData.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    _handleSaveSubjectLiveData.value = ApiResponses.error(t.message.toString())
                }
            })
    }
}