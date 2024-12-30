package com.ikhsannurhakiki.aplikasiforum.data.source.remote

import PagingDataSource
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.service.ApiConfig
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.service.ApiConfig2
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RemoteDataSource private constructor(private val apiConfig: ApiConfig) {

    companion object {

        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(apiConfig: ApiConfig): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource(apiConfig).apply { instance = this }
            }
    }


    //++++++++++++++++++++++++++++++++++++QUESTION+++++++++++++++++++++++++++++++
    fun insertQuestion(
        idAskedBy: String,
        title: String,
        question: String,
        subjectId: Int,
        lecturerId: String?,
        listTags: List<String>,
        supplecturer: Int,
        materialId: Int
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().insertQuestion(
            "insertQuestion2",
            idAskedBy,
            title,
            question,
            subjectId,
            lecturerId,
            listTags,
            supplecturer,
            materialId
        )
            .enqueue(
                object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {
                        if (response.code() == 200) {
                            response.body()?.let {
                                result.value = ApiResponses.success(it)
                            }
                        } else {
                            result.value = ApiResponses.error(response.code().toString())
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        result.value = ApiResponses.error("Error")
                    }

                })
        return result
    }

    fun getAllQuestion(
        suppLecturer: Int,
        code: Int,
        key: String,
        materialId: Int
    ): LiveData<PagingData<QuestionTagResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 1,
                enablePlaceholders = false,
                initialLoadSize = 2
            ),
            pagingSourceFactory = {
                PagingDataSource(apiConfig.client(), suppLecturer, code, key, materialId)
            }, initialKey = 1
        ).liveData
    }

    fun getDetailQuestion(questionId: Int): LiveData<ApiResponses<QuestionTagResponse>> {
        val detailQuestion = MutableLiveData<ApiResponses<QuestionTagResponse>>()

        apiConfig.client().getDetailQuestion("getDetailQuestion", questionId)
            .enqueue(object : Callback<QuestionTagResponse> {
                override fun onResponse(
                    call: Call<QuestionTagResponse>,
                    response: Response<QuestionTagResponse>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                detailQuestion.value = ApiResponses.success(it)
                            } else {
                                detailQuestion.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }
                    } else {
                        detailQuestion.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(call: Call<QuestionTagResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return detailQuestion
    }

    fun updateQuestionViews(questionId: Int, views: Int) {
        apiConfig.client().updateQuestionViews(
            "updateQuestionViews",
            questionId,
            views
        ).enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.i(TAG, "onSuccesss: ${response.message()}")
            }
        })
    }

    fun getAllMyQuestion(userId: String): LiveData<ApiResponses<List<QuestionTagResponse>>> {
        val myQuestion = MutableLiveData<ApiResponses<List<QuestionTagResponse>>>()
        apiConfig.client().getAllMyQuestion(
            "getAllMyQuestion",
            userId
        ).enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(
                call: Call<QuestionResponse>,
                response: Response<QuestionResponse>
            ) {
                if (response.code() == 200) {
                    response.body()?.result?.let {
                        if (it.isNotEmpty()) {
                            myQuestion.value = ApiResponses.success(it)
                        } else {
                            myQuestion.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    myQuestion.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return myQuestion
    }

    //++++++++++++++++++++++++++++++++++++ANSWER+++++++++++++++++++++++++++++++
    fun getAnswers(questionId: Int, userId: String): LiveData<ApiResponses<List<Answer>>> {
        val answer = MutableLiveData<ApiResponses<List<Answer>>>()
        apiConfig.client().getAnswers(
            "getAnswers",
            questionId,
            userId
        ).enqueue(object : Callback<AnswerResponse> {
            override fun onResponse(
                call: Call<AnswerResponse>,
                response: Response<AnswerResponse>
            ) {
                if (response.code() == 200) {
                    response.body()?.message?.let {
                        if (it != "EMPTY") {
                            response.body()?.result?.let {
                                answer.value = ApiResponses.success(it)
                            }
                        } else {
                            answer.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    answer.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return answer
    }

    //+++++++++++++++++++++++++++++++SCORE++++++++++++++++++++++++++++++++++++++
    fun votingQuestion(scoreResponse: ScoreResponse, voting: String) {
        when (voting) {
            "Insert" -> {
                apiConfig.client().insertVoting(
                    "votingQuestion",
                    voting,
                    scoreResponse.votedId,
                    scoreResponse.voteById,
                    scoreResponse.score
                )
                    .enqueue(
                        object : Callback<ScoreResponse> {
                            override fun onResponse(
                                call: Call<ScoreResponse>,
                                response: Response<ScoreResponse>
                            ) {
                                if (response.code() == 200) {
                                    Log.i(TAG, "onSuccess: ${response.message()}")
                                } else {
                                    Log.e(TAG, "onFailure: ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                                Log.e(TAG, "onFailure: ${t.message.toString()}")
                            }

                        })
            }
            "Delete" -> {
                apiConfig.client().deleteVoting(
                    "votingQuestion",
                    voting,
                    scoreResponse.votedId,
                    scoreResponse.voteById,
                    scoreResponse.score
                )
                    .enqueue(
                        object : Callback<ScoreResponse> {
                            override fun onResponse(
                                call: Call<ScoreResponse>,
                                response: Response<ScoreResponse>
                            ) {
                                if (response.code() == 200) {
                                    Log.i(TAG, "onSuccess: ${response.message()}")
                                } else {
                                    Log.e(TAG, "onFailure: ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                                Log.e(TAG, "onFailure: ${t.message.toString()}")
                            }

                        })
            }
        }

    }

    fun checkVotedOrNot(questionId: Int, userId: String?): LiveData<ApiResponses<ScoreResponse>> {
        val voted = MutableLiveData<ApiResponses<ScoreResponse>>()

        apiConfig.client().checkVotedOrNot("checkVotedOrNot", questionId, userId)
            .enqueue(object : Callback<ScoreResponse> {
                override fun onResponse(
                    call: Call<ScoreResponse>,
                    response: Response<ScoreResponse>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                voted.value = ApiResponses.success(it)
                            } else {
                                voted.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }

                    } else {
                        voted.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return voted
    }

    fun checkPoint(questionId: Int): LiveData<ApiResponses<Int>> {
        val voted = MutableLiveData<ApiResponses<Int>>()

        apiConfig.client().checkPoint("checkPoint", questionId)
            .enqueue(object : Callback<Int> {
                override fun onResponse(
                    call: Call<Int>,
                    response: Response<Int>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                voted.value = ApiResponses.success(it)
                            } else {
                                voted.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }

                    } else {
                        voted.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return voted
    }

    //++++++++++++++++++++++++++++++++USER+++++++++++++++++++++++++++++++++++++++
    fun insertStudent(user: Student) {
        Log.d("token2", user.deviceToken)
        apiConfig.client().insertStudent(
            "insertUser",
            user.id,
            user.name,
            user.email,
            user.accessRights,
            user.npm,
            user.deviceToken
        )
            .enqueue(
                object :
                    Callback<Student> {
                    override fun onResponse(
                        call: Call<Student>,
                        response: Response<Student>
                    ) {
                        if (response.code() == 200) {
                            Log.i(TAG, "onSuccess: ${response.message()}")
                        } else {
                            Log.e(TAG, "onFailure: ${response.message()}")
                        }
                    }

                    override fun onFailure(
                        call: Call<Student>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "onFailure: ${t.message.toString()}")
                    }
                })
    }

    fun getDetailLecturer(userId: String): LiveData<ApiResponses<Lecturer>> {
        val detailUser = MutableLiveData<ApiResponses<Lecturer>>()
        apiConfig.client().getDetailLecturer("getDetailLecturer", userId)
            .enqueue(object :
                Callback<Lecturer> {
                override fun onResponse(
                    call: Call<Lecturer>,
                    response: Response<Lecturer>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                detailUser.value = ApiResponses.success(it)
                            } else {
                                detailUser.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }

                    } else {
                        detailUser.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(
                    call: Call<Lecturer>,
                    t: Throwable
                ) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return detailUser
    }

    fun getDetailStudent(userId: String): LiveData<ApiResponses<Student>> {
        val detailUser = MutableLiveData<ApiResponses<Student>>()

        apiConfig.client().getDetailStudent("getDetailStudent", userId)
            .enqueue(object :
                Callback<Student> {
                override fun onResponse(
                    call: Call<Student>,
                    response: Response<Student>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                detailUser.value = ApiResponses.success(it)
                            } else {
                                detailUser.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }

                    } else {
                        detailUser.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(
                    call: Call<Student>,
                    t: Throwable
                ) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return detailUser
    }

    fun checkAccessRight(userId: String): LiveData<ApiResponses<String>> {
        val detailUser = MutableLiveData<ApiResponses<String>>()

        apiConfig.client().checkAccess("checkAccess", userId)
            .enqueue(object :
                Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                detailUser.value = ApiResponses.success(it)
                            } else {
                                detailUser.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }

                    } else {
                        detailUser.value = ApiResponses.error("ERROR_CONNECTION")
                    }
                }

                override fun onFailure(
                    call: Call<String>,
                    t: Throwable
                ) {
                    Log.e(TAG, "onFailure: ${t.message.toString()}")
                }
            })
        return detailUser
    }

    fun updateDetailUser(user: EditUser) {
        apiConfig.client().editDetailUser(
            "updateDetailUser",
            user.id,
            user.name,
            user.email,
            user.info
        )
            .enqueue(
                object :
                    Callback<EditUser> {
                    override fun onResponse(
                        call: Call<EditUser>,
                        response: Response<EditUser>
                    ) {
                        if (response.code() == 200) {

                            Log.i(TAG, "onSuccess: ${response.message()}")
                        } else {
                            Log.e(TAG, "onFailure: ${response.message()}")
                        }
                    }

                    override fun onFailure(
                        call: Call<EditUser>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "onFailure: ${t.message.toString()}")
                    }

                })
    }

    //++++++++++++++++++++++++++++++++++++SUBJECT+++++++++++++++++++++++++++++++
    fun getSubject(): LiveData<ApiResponses<List<Subject>>> {
        val subject = MutableLiveData<ApiResponses<List<Subject>>>()
        apiConfig.client().getSubject(
            "getSubject"
        ).enqueue(object : Callback<List<Subject>> {
            override fun onResponse(
                call: Call<List<Subject>>,
                response: Response<List<Subject>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            subject.value = ApiResponses.success(it)
                        } else {
                            subject.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    subject.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        subject.value = ApiResponses.error("ERROR_CONNECTION")
        return subject
    }

    fun getLecturerSubject(lecturerId: String): LiveData<ApiResponses<List<Subject>>> {
        val subject = MutableLiveData<ApiResponses<List<Subject>>>()
        apiConfig.client().getLecturerSubject(
            "getLecturerSubject",
            lecturerId
        ).enqueue(object : Callback<List<Subject>> {
            override fun onResponse(
                call: Call<List<Subject>>,
                response: Response<List<Subject>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            subject.value = ApiResponses.success(it)
                        } else {
                            subject.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    subject.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return subject
    }


    //++++++++++++++++++++++++++++++++++++LECTURER+++++++++++++++++++++++++++++++

    fun insertLecturer(lecturer: Lecturer) {
        Log.d("token2", lecturer.deviceToken)
        apiConfig.client().insertLecturer(
            "insertUser",
            lecturer.id,
            lecturer.name,
            lecturer.email,
            lecturer.info,
            lecturer.accessRights,
            lecturer.nip,
            lecturer.deviceToken
        ).enqueue(object : Callback<Lecturer> {
            override fun onResponse(call: Call<Lecturer>, response: Response<Lecturer>) {
                if (response.code() == 200) {
                    Log.i(TAG, "onSuccess: ${response.message()}")
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Lecturer>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
    }

    fun getLecturerBySubject(subjectId: Int): LiveData<ApiResponses<List<Lecturer>>> {
        val lecturer = MutableLiveData<ApiResponses<List<Lecturer>>>()
        apiConfig.client().getLecturerBySubject(
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
                            lecturer.value = ApiResponses.success(it)
                        } else {
                            lecturer.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    lecturer.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<Lecturer>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return lecturer
    }

    fun insertLecturerSubject(subjectList: Int, lecturerId: String): LiveData<ApiResponses<String>> {
        var result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().insertLecturerSubject(
            "insertLecturerSubject",
            subjectList,
            lecturerId
        ).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.code() == 200) {
                    Log.i(TAG, "onSuccess: ${response.message()}")
                    result.value = ApiResponses.success("Success")
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }

    fun updateUserDeviceToken(uid: String, token: String?) {
        apiConfig.client().updateUserDeviceToken(
            "updateUserDeviceToken",
            uid,
            token!!
        )
            .enqueue(
                object :
                    Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {
                        if (response.code() == 200) {

                            Log.i(TAG, "onSuccess: ${response.message()}")
                        } else {
                            Log.e(TAG, "onFailure: ${response.message()}")
                        }
                    }

                    override fun onFailure(
                        call: Call<Unit>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "onFailure: ${t.message.toString()}")
                    }

                })
    }

    fun getCurrentAutoIncrement(): LiveData<ApiResponses<Int>> {
        val currentAutoIncrement = MutableLiveData<ApiResponses<Int>>()
        apiConfig.client().getCurrentAutoIncrement(
            "getCurrentAutoIncrement"
        ).enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            currentAutoIncrement.value = ApiResponses.success(it)
                        } else {
                            currentAutoIncrement.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    currentAutoIncrement.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return currentAutoIncrement
    }

    fun checkTags(tag: String): LiveData<ApiResponses<String>> {
        val checkTags = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().checkTags(
            "checkTags",
            tag
        ).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            checkTags.value = ApiResponses.success(it)
                        } else {
                            checkTags.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    checkTags.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return checkTags
    }

    fun insertTags(tag: String) {
        apiConfig.client().insertTag(
            "insertTag",
            tag
        ).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.code() == 200) {
                    Log.i(TAG, "onSuccess: ${response.message()}")
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
    }

    fun insertQuestionTags(questionId: String, tagId: String) {
        apiConfig.client().insertQuestionTag(
            "insertQuestionTag2",
            questionId,
            tagId
        ).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.code() == 200) {
                    Log.i(TAG, "onSuccess: ${response.message()}")
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
    }

    fun getTagByQuestion(questionId: Int): LiveData<ApiResponses<List<TagResponse>>> {
        val tag = MutableLiveData<ApiResponses<List<TagResponse>>>()
        apiConfig.client().getTagbyQuestion(
            "getTagByQuestion",
            questionId
        ).enqueue(object : Callback<List<TagResponse>> {
            override fun onResponse(
                call: Call<List<TagResponse>>,
                response: Response<List<TagResponse>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            tag.value = ApiResponses.success(it)
                        } else {
                            tag.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    tag.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<List<TagResponse>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return tag
    }

    fun finishQuestion(questionId: Int): LiveData<ApiResponses<Int>> {
        val status = MutableLiveData<ApiResponses<Int>>()
        apiConfig.client().finishQuestion(
            "finishQuestion",
            questionId
        )
            .enqueue(
                object :
                    Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {
                        if (response.code() == 200) {
                            status.value = ApiResponses.success(response.code())
                            Log.i(TAG, "onSuccess: ${response.message()}")
                        } else {
                            Log.e(TAG, "onFailure: ${response.message()}")
                        }
                    }

                    override fun onFailure(
                        call: Call<Unit>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "onFailure: ${t.message.toString()}")
                    }

                })
        Log.i("TAG2", "onSuccess: $status")
        return status
    }

    fun getToken(userId: String?): LiveData<ApiResponses<String>> {
        val token = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().getToken(
            "getToken",
            userId
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            token.value = ApiResponses.success(it)
                        } else {
                            token.value = ApiResponses.empty("EMPTY_DATA")
                        }
                    }
                } else {
                    token.value = ApiResponses.error("ERROR_CONNECTION")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
        return token
    }

    fun uploadImages(
        images: MultipartBody.Part,
        lastQuestionId: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().uploadImages(
            "uploadImages",
            images, lastQuestionId, isQuestion
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    result.value = ApiResponses.success(response.body().toString())
                } else {
                    result.value = response.body()?.let { ApiResponses.error(it) }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
        return result
    }

    fun getQuestionsImage(questionId: Int): LiveData<ApiResponses<List<ImagesResponse>>> {
        val result = MutableLiveData<ApiResponses<List<ImagesResponse>>>()
        apiConfig.client().getQuestionsImage(
            "getQuestionsImage",
            questionId
        ).enqueue(object : Callback<List<ImagesResponse>> {
            override fun onResponse(
                call: Call<List<ImagesResponse>>,
                response: Response<List<ImagesResponse>>
            ) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        response.body()?.let {
                            result.value = ApiResponses.success(it)
                        }
                    } else {
                        result.value = ApiResponses.error(response.code().toString())
                    }
                }
            }

            override fun onFailure(call: Call<List<ImagesResponse>>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

        })
        return result
    }

    fun insertAnswer(
        answer: String,
        uid: String?,
        questionId: Int
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().insertAnswer(
            "insertAnswer",
            answer,
            uid,
            questionId
        )
            .enqueue(
                object : Callback<GeneralResponse> {
                    override fun onResponse(
                        call: Call<GeneralResponse>,
                        response: Response<GeneralResponse>
                    ) {
                        if (response.code() == 200) {
                            response.body()?.let {
                                result.value = ApiResponses.success(it.status)
                            }
                        } else {
                            result.value = ApiResponses.error(response.code().toString())
                        }
                    }

                    override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                        result.value = ApiResponses.error("Error")
                    }

                })
        return result
    }

    fun insertBookmark(
        questionId: Int,
        userId: String,
        status: String
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().insertBookmark(
            "bookmarkHandler",
            userId,
            questionId,
            status
        ).enqueue(
            object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.code() == 200) {
                        result.value = ApiResponses.success("Success")
                    } else {
                        result.value = ApiResponses.error("Error")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    result.value = ApiResponses.error("Error")
                }

            }
        )
        return result
    }

    fun checkBookmark(questionId: Int, userId: String): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().checkBookmark(
            "checkBookmark",
            userId,
            questionId
        ).enqueue(
            object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                result.value = ApiResponses.success(it)
                            } else {
                                result.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    result.value = ApiResponses.error("Error")
                }

            }
        )
        return result
    }

    fun verifiedAnswerHandler(
        answerId: Int,
        isVerified: Int
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().verifiedAnswerHandler(
            "verifiedAnswerHandler",
            answerId,
            isVerified
        ).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                result.value = ApiResponses.success(response.body().toString())
            }
        })
        return result
    }

    fun insertReport(
        message: String,
        objectReported: Int,
        reporter: String,
        isQuestion: String
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().reportHandler(
            "insertReportHandler",
            message,
            reporter,
            objectReported,
            isQuestion
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    result.value = ApiResponses.success("Success")
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = ApiResponses.error("Error")
            }
        })
        return result
    }

    fun deleteQAHandler(deletingId: Int, isQuestion: Boolean): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().deleteQAHandler(
            "deleteQAHandler",
            deletingId,
            isQuestion
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    if (response.body() == "Success") {
                        result.value = ApiResponses.success("Success")
                    } else {
                        result.value = ApiResponses.error("Error")
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = ApiResponses.error("Error")
            }
        })
        return result
    }

    fun getAllReportedList(
        userId: String,
        isQuestion: String
    ): LiveData<ApiResponses<List<Report>>> {
        val result = MutableLiveData<ApiResponses<List<Report>>>()
        apiConfig.client().getAllReportedList(
            "getAllReportedList",
            userId,
            isQuestion
        ).enqueue(
            object : Callback<List<Report>> {
                override fun onResponse(
                    call: Call<List<Report>>,
                    response: Response<List<Report>>
                ) {
                    Log.d("resss", response.code().toString())
                    if (response.code() == 200) {
                        response.body().let {
                            if (it != null) {
                                result.value = ApiResponses.success(it)
                            } else {
                                result.value = ApiResponses.empty("EMPTY_DATA")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                    Log.d("resss", t.message.toString())
                    result.value = ApiResponses.error("Tst")
                }

            }
        )
        return result
    }

    fun getSuppLecturerId(lecturerId: String, subjectId: Int): LiveData<ApiResponses<Int>> {
        val result = MutableLiveData<ApiResponses<Int>>()
        ApiConfig2.getApiService().getSuppLecturerId(
            "getSuppLecturerId",
            lecturerId,
            subjectId
        ).enqueue(object : Callback<Int> {
            override fun onResponse(p0: Call<Int>, response: Response<Int>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.error("Error")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(p0: Call<Int>, p1: Throwable) {
                result.value = ApiResponses.error("Error")
            }
        })
        return result
    }

    fun insertPunishment(
        reportid: Int,
        type: String,
        formattedDate: String,
        note: String,
        reportType: String
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        ApiConfig2.getApiService().insertPunishment(
            "insertPunishment",
            reportid,
            type,
            formattedDate,
            note,
            reportType
        ).enqueue(object : Callback<String> {
            override fun onResponse(p0: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.error("Error")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(p0: Call<String>, p1: Throwable) {
                result.value = ApiResponses.error("Error")
            }
        })
        return result
    }

    fun checkPunishment(
        suppLecturer: Int,
        userId: String
    ): LiveData<ApiResponses<List<PunishmentResponse>>> {
        val result = MutableLiveData<ApiResponses<List<PunishmentResponse>>>()
        ApiConfig2.getApiService().checkPunishment(
            "checkPunishment",
            suppLecturer,
            userId
        ).enqueue(object : Callback<List<PunishmentResponse>> {
            override fun onResponse(
                p0: Call<List<PunishmentResponse>>,
                response: Response<List<PunishmentResponse>>
            ) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(p0: Call<List<PunishmentResponse>>, p1: Throwable) {
                result.value = ApiResponses.error(p1.message.toString())
                Log.d("bbb", p1.message.toString())
            }
        })
        return result
    }

    fun checkIsAnswerVoted(answerId: Int, userId: String): LiveData<ApiResponses<ScoreResponse>> {
        val result = MutableLiveData<ApiResponses<ScoreResponse>>()
        apiConfig.client().checkIsAnswerVoted(
            "checkIsAnswerVoted",
            answerId,
            userId
        ).enqueue(object : Callback<ScoreResponse> {
            override fun onResponse(call: Call<ScoreResponse>, response: Response<ScoreResponse>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("Empty")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }

    fun answerVotingHandler(
        userId: String,
        score: Int,
        answerId: Int,
        f: String
    ): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().answerVotingHandler(
            "answerVotingHandler",
            userId,
            score,
            answerId,
            f
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("Empty")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }

    fun declineReport(reportId: Int, reason: String): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().declineReport(
            "declineReport",
            reportId,
            reason
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("Empty")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }

    fun deleteReportDecision(reportId: Int, status: String, note: String): LiveData<ApiResponses<String>> {
        val result = MutableLiveData<ApiResponses<String>>()
        apiConfig.client().deleteReportDecision(
            "deleteReportDecision",
            reportId,
            status,
            note
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("Empty")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }

    fun getReportedData(reportId: Int, type: String): LiveData<ApiResponses<QuestionReportedData>> {
        val result = MutableLiveData<ApiResponses<QuestionReportedData>>()
        apiConfig.client().getReportedData(
            "getReportedData",
            reportId,
            type
        ).enqueue(object : Callback<QuestionReportedData> {
            override fun onResponse(call: Call<QuestionReportedData>, response: Response<QuestionReportedData>) {
                if (response.code() == 200) {
                    response.body().let {
                        if (it != null) {
                            result.value = ApiResponses.success(it)
                        } else {
                            result.value = ApiResponses.empty("Empty")
                        }
                    }
                } else {
                    result.value = ApiResponses.error("Error")
                }
            }

            override fun onFailure(call: Call<QuestionReportedData>, t: Throwable) {
                result.value = ApiResponses.error(t.message.toString())
            }
        })
        return result
    }
}
