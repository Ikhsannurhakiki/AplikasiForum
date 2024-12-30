package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.AnswerAdapter
import com.ikhsannurhakiki.aplikasiforum.adapter.TagAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.local.entity.BookmarkedEntity
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.ScoreResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentDetailForumBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.io.File
import java.io.IOException

class DetailForumFragment : Fragment(), ForumInterface, AnswerInterface, ImageInterface {

    private lateinit var _binding: FragmentDetailForumBinding
    private val binding get() = _binding
    private var questionId: Int = 0
    private var question: QuestionTagResponse? = null
    private lateinit var auth: FirebaseAuth
    private var answerList: ArrayList<Answer> = arrayListOf()
    private var lecturerId: String? = null
    private var subjectId: Int = 0
    private var subjectName: String? = null
    private var point: Int = 0
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailForumBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLoading(true)

        factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        binding.itemContainer.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        arguments?.let { arg ->
            subjectId = arg.getInt(HomeFragment.SUBJECTID)
            lecturerId = arg.getString(HomeFragment.LECTURERID)
            questionId = arg.getInt(QUESTIONID)
            question = arg.getParcelable(QUESTION)
            subjectName = arg.getString(SUBJECTNAME)
        }

        getQuestionData()
        checkAccessRightPoint()

        binding.btnFinish.setOnClickListener {
            finishQuestion()
        }
    }

    private fun finishQuestion() {
        viewModel.finishQuestion(questionId).observe(viewLifecycleOwner) { detailQuestion ->
            when (detailQuestion.status) {
                StatusResponses.SUCCESS -> {
                    isQAFinish(true)
                }
                StatusResponses.ERROR -> {
                    isQAFinish(true)
                }
            }
        }
    }

    private fun isQAFinish(status: Boolean) {
        if (status) {
            Snackbar.make(
                requireView(),
                "Tanya jawab telah selesai",
                Snackbar.LENGTH_SHORT
            ).show()
            binding.btnFinish.text = "Selesai"
        } else {
            binding.btnFinish.text = "Belum Selesai"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getQuestionData() {

        auth = FirebaseAuth.getInstance()
        val userId = "'" + auth.currentUser?.uid + "'"

        viewModel.getDetailQuestion(questionId).observe(viewLifecycleOwner) { detailQuestion ->
            when (detailQuestion.status) {
                StatusResponses.SUCCESS -> {
                    binding.apply {
                        isLoading(false)
                        val tagAdapter = detailQuestion?.body?.tag?.let {
                            TagAdapter(
                                it,
                                this@DetailForumFragment,
                                subjectId,
                                lecturerId
                            )
                        }
                        rvTag.setHasFixedSize(true)
                        rvTag.adapter = tagAdapter
                        rvTag.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


                        val ref =
                            FirebaseStorage.getInstance().reference.child("img/${detailQuestion.body?.askedBy}")
                        try {
                            val localFile = File.createTempFile("tempImage", ".jpeg")
                            ref.getFile(localFile)
                                .addOnSuccessListener {
                                    val bitmap =
                                        BitmapFactory.decodeFile(localFile.absolutePath)
                                    binding.circleImageView.setImageBitmap(bitmap)
                                }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        detailQuestion.body?.let {
                            tvAskedBy.text = it.name
                            tvTitle.text = it.title
                            tvQuestion.text = it.question
                            tvViews.text =
                                "${resources.getString(R.string.views)} : ${it.views} x"
                            tvScore.text = it.totalPoint.toString()
                            val timeAgo = calculateTimeAgo(it.date)
                            tvTimeAgo.text = "${resources.getString(R.string.posted)} : $timeAgo"


                            if (it.status == "selesai") {
                                isQAFinish(true)
                            } else {
                                isQAFinish(false)
                            }

                            checkVotedOrNot(
                                it.id,
                                userId,
                                it.totalPoint
                            )

                            try {
                                displayAnswerData(it.id)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }


//
//                        val timeAgoEdited =
//                            detailQuestion.body?.let { calculateTimeAgo(it.editedDate) }


//                        binding.rvTag.layoutManager = LinearLayoutManager(context)
//                        binding.rvTag.setHasFixedSize(true)
//                        binding.rvTag.adapter = detailQuestion?.body?.tag?.let { TagAdapter(it) }

                    }
                }

                StatusResponses.EMPTY -> Toast.makeText(
                    context,
                    resources.getString(R.string.dataEmpty),
                    Toast.LENGTH_SHORT
                ).show()

                StatusResponses.ERROR -> Toast.makeText(
                    context,
                    resources.getString(R.string.error),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun checkVotedOrNot(questionId: Int?, userId: String?, score: Int?) {
        var voting = "Insert"

        if (questionId != null && userId != null) {
            viewModel.checkVotedOrNot(questionId, userId).observe(viewLifecycleOwner) { check ->
                when (check.status) {
                    StatusResponses.SUCCESS -> {
                        if (check.body?.score == 1 || check.body?.score == 5) { //JIKA SUDAH VOTE UP
                            binding.btnScoreUp.isSelected = true
                            binding.btnScoreDown.isSelected = false
                            binding.btnScoreUp.setOnClickListener {
                                checkAccessRightPoint()
                                var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                                when (txtScore) {
                                    score -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        binding.btnScoreDown.isSelected = false
                                        point *= -1
                                        voting = "Delete"
                                    }
                                    score!! - point -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = true
                                        point *= 1
                                        voting = "Insert"
                                    }
                                    score - 2 -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        binding.btnScoreDown.isSelected = false
                                        point *= 1
                                        voting = "Delete"
                                    }
                                }
                                votingQuestion(voting, questionId, point)
                                snackBarShow(voting)
                            }
                            binding.btnScoreDown.setOnClickListener {
                                checkAccessRightPoint()
                                var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                                when (txtScore) {
                                    score -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        point *= -1
                                        voting = "Delete"
                                    }
                                    score!! - point -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreDown.isSelected = true
                                        point *= -1
                                        voting = "Insert"
                                    }
                                    score - 2 -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreDown.isSelected = false
                                        point *= 1
                                        voting = "Delete"
                                    }
                                }
                                votingQuestion(voting, questionId, point)
                                snackBarShow(voting)
                            }
                        } else if (check.body?.score == -1 || check.body?.score == -5) { //JIKA SUDAH VOTE DOWN
                            binding.btnScoreUp.isSelected = false
                            binding.btnScoreDown.isSelected = true
                            binding.btnScoreDown.setOnClickListener {
                                checkAccessRightPoint()
                                var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                                when (txtScore) {
                                    score -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        binding.btnScoreDown.isSelected = false
                                        point *= 1
                                        voting = "Delete"
                                    }
                                    score!! + 1 -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreDown.isSelected = true
                                        point *= -1
                                        voting = "Insert"
                                    }
                                    score + 2 -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        binding.btnScoreDown.isSelected = false
                                        point *= -1
                                        voting = "Delete"
                                    }
                                }
                                votingQuestion(voting, questionId, point)
                                snackBarShow(voting)
                            }
                            binding.btnScoreUp.setOnClickListener {
                                checkAccessRightPoint()
                                var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                                when (txtScore) {
                                    score -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreDown.isSelected = false
                                        point *= 1
                                        voting = "Delete"
                                    }
                                    score!! + 1 -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = true
                                        point *= 1
                                        voting = "Insert"
                                    }
                                    score + 2 -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = false
                                        point *= 1
                                        voting = "Delete"
                                    }
                                }
                                votingQuestion(voting, questionId, point)
                                snackBarShow(voting)
                            }
                        }
                    }
                    StatusResponses.EMPTY -> {
                        binding.btnScoreUp.isSelected = false
                        binding.btnScoreDown.isSelected = false
                        binding.btnScoreUp.setOnClickListener {
                            checkAccessRightPoint()
                            var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                            when (txtScore) {
                                score -> {
                                    txtScore += point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreUp.isSelected = true
                                    point *= 1
                                    voting = "Insert"
                                }
                                score!! + point -> {
                                    txtScore -= point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreUp.isSelected = false
                                    point *= -1
                                    voting = "Delete"
                                }
                                score - point -> {
                                    txtScore += point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreDown.isSelected = false
                                    point *= 1
                                    voting = "Delete"
                                }
                            }
                            Log.d("Point", point.toString())
                            votingQuestion(voting, questionId, point)
                            snackBarShow(voting)
                        }
                        binding.btnScoreDown.setOnClickListener {
                            checkAccessRightPoint()
                            var txtScore = Integer.parseInt(binding.tvScore.text.toString())
                            when (txtScore) {
                                score -> {
                                    txtScore -= point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreDown.isSelected = true
                                    point *= -1
                                    voting = "Insert"
                                }
                                score!! + point -> {
                                    txtScore -= point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreUp.isSelected = false
                                    point *= -1
                                    voting = "Delete"
                                }
                                score - point -> {
                                    txtScore += point
                                    binding.tvScore.text = txtScore.toString()
                                    binding.btnScoreDown.isSelected = false
                                    point *= 1
                                    voting = "Delete"
                                }
                            }
                            votingQuestion(voting, questionId, point)
                            snackBarShow(voting)
                        }
                    }

                    StatusResponses.ERROR -> Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun checkAccessRightPoint() {
        auth = FirebaseAuth.getInstance()
        val accessRight = viewModel.checkAccessRight(auth.currentUser?.uid.toString())
        when (accessRight.value?.body) {
            "Dosen" -> 5
            else -> 1
        }

    }

    private fun snackBarShow(voting: String) {
        var snackBar = ""
        when (voting) {
            "Insert" -> {
                snackBar = getString(R.string.snackbarThanks)
            }
            "Delete" -> {
                snackBar = getString(R.string.snackbarDelete)
            }
        }
        Snackbar.make(
            requireView(),
            snackBar,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun displayAnswerData(questionId: Int?) {

        if (questionId != null) {
            auth.currentUser?.let {
                viewModel.getAnswer(questionId, it.uid).observe(viewLifecycleOwner) { answer ->
                    when (answer.status) {
                        StatusResponses.SUCCESS -> {
                            binding.rvAnswer.setHasFixedSize(true)
                            binding.rvAnswer.layoutManager = LinearLayoutManager(activity)
                            answer.body?.let {
                                answerList.addAll(it)
                                val forumAdapter = AnswerAdapter(it, this, this, this,"",point, requireContext())
                                binding.rvAnswer.adapter = forumAdapter
                                forumAdapter.notifyDataSetChanged()
                            }
                        }
                        StatusResponses.ERROR -> {
                            Toast.makeText(
                                context,
                                StatusResponses.ERROR.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        StatusResponses.EMPTY -> {
                            Toast.makeText(context, "Data tidak tersedia", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }
    }

    private fun votingQuestion(voting: String, questionId: Int, point: Int) {

        val uid = auth.currentUser?.uid!!
        val response = ScoreResponse(
            questionId,
            uid,
            point
        )
        viewModel.votingQuestion(response, voting)
    }

    private fun isLoading(b: Boolean) {
        binding.apply {
            if (b) {
                progressBar.visibility = View.VISIBLE
                layout.visibility = View.GONE

            } else {
                progressBar.visibility = View.GONE
                layout.visibility = View.VISIBLE
                itemContainer.visibility = View.VISIBLE
            }
        }
    }

    fun bookmarkingQuestion(bookmarkedEntity: BookmarkedEntity) {

    }

    override fun click(question: QuestionTagResponse) {
        TODO("Not yet implemented")
    }

    override fun onImageClicked(userId: String) {
        TODO("Not yet implemented")
    }

    override fun onTagClicked(tag: String, subjectId: Int, lecturerId: String?) {
        val bundle = Bundle()
//        bundle.putParcelableArray(DetailForumFragment.QUESTION, question)
        bundle.putInt(HomeFragment.SUBJECTID, subjectId)
        bundle.putString(HomeFragment.LECTURERID, lecturerId)
        bundle.putBoolean(HomeFragment.TAGCLICKED.toString(), true)
        bundle.putString(HomeFragment.TAG, tag)
        findNavController().navigate(R.id.navigation_home, bundle)
    }

    override fun answerThreeDotsClicked(answer: Answer) {
        TODO("Not yet implemented")
    }

    companion object {
        const val SUBJECTNAME = "subjectname"
        const val LECTURERID = "lecturerid"
        const val SUBJECTID = "1"
        const val QUESTIONID = "questionid"
        const val QUESTION = "question"
    }



    override fun voteHandler(answerId: Int, s: String, score: Int) {
        TODO("Not yet implemented")
    }

    override fun onImageClicked(uri: Uri) {
        TODO("Not yet implemented")
    }
}

