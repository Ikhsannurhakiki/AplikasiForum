package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.Nullable
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.BuildConfig
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.AnswerAdapter
import com.ikhsannurhakiki.aplikasiforum.adapter.ImageSliderAdapterUri
import com.ikhsannurhakiki.aplikasiforum.adapter.TagAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.*
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityDetailQuestionBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.LayoutAnswerThreeDotsClickedBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.io.File
import java.io.IOException

class DetailQuestionActivity : AppCompatActivity(), ForumInterface, AnswerInterface,
    ImageInterface {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityDetailQuestionBinding
    private lateinit var _binding2: LayoutAnswerThreeDotsClickedBinding
    private val binding2 get() = _binding2
    private lateinit var imageAdapter: ImageSliderAdapterUri
    private lateinit var dots: ArrayList<TextView>
    private var questionId: Int = 0
    private lateinit var auth: FirebaseAuth
    private var answerList: ArrayList<Answer> = arrayListOf()
    private var lecturerId: String? = null
    private var subjectId: Int = 0
    private var askedById: String? = null
    private var subjectName: String? = null
    private var isPunished: String? = null
    private var point: Int = 0
    private val getFile = ArrayList<ImageFileListUri>()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userId: String
    private lateinit var questionStatus: String
    private val ADD_DATA_REQUEST_CODE = 1
    private lateinit var answerAdapter: AnswerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLoading(true, "Memuat data...")

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val pref = UserPreferences.getInstance(dataStore)
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser!!.uid

        intent.let {
            subjectId = it.getIntExtra(SUBJECTID, 0)
            lecturerId = it.getStringExtra(LECTURERID)
            askedById = it.getStringExtra(ASKEDBYID)
            questionId = it.getIntExtra(QUESTIONID, 0)
            questionStatus = it.getStringExtra(QUESTIONSTATUS).toString()
            subjectName = it.getStringExtra(SUBJECTNAME)
            isPunished = it.getStringExtra(ISPUNISHED)
        }
        checkPunishment()
        setQuestionStatusLabel(questionStatus)
        getImages()
        getQuestionData()
        checkBookmark()
        checkAccessRightPoint()

        binding.btnBookmark.setOnClickListener {
            val isSelected = binding.btnBookmark.isSelected
            viewModel.insertBookmark(questionId, userId, (!isSelected).toString()).observe(this) {
                when (it.status) {
                    StatusResponses.SUCCESS -> {
                        if (isSelected) showToast("Dihapus dari pertanyaan yang ditandai")
                        else showToast("Ditambahkan ke pertanyaan yang ditandai")
                        binding.btnBookmark.isSelected = !isSelected
                    }
                    StatusResponses.EMPTY -> showToast("Terjadi kesalahan")
                    StatusResponses.ERROR -> showToast("Terjadi kesalahan")
                }
            }
        }
    }

    private fun checkPunishment() {
        if (isPunished == "Tipe 1") {
            Toast.makeText(
                this,
                "Anda tidak bisa berinteraksi di kelas ini (Pelanggarann Tipe 1)",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnScoreUp.isEnabled = false
            binding.btnScoreDown.isEnabled = false
            binding.btnBookmark.isEnabled = false
        }
    }

    private fun getImages() {
        viewModel.getQuestionsImage(questionId).observe(this) { image ->
            when (image.status) {
                StatusResponses.SUCCESS -> {
                    image.body.let {
                        if (it != null) {
                            for (element in it) {
                                val uri =
                                    Uri.parse(BuildConfig.BASE_URL + "/uploads/question/" + element.images)
                                getFile.add(
                                    ImageFileListUri(uri)
                                )
                            }
                            showImage()
                        }
                    }
                }
                StatusResponses.EMPTY -> {
                    binding.dotsIndicator.isVisible = false
                    binding.viewPager.isVisible = false
                }
                StatusResponses.ERROR -> {
                    binding.dotsIndicator.isVisible = false
                    binding.viewPager.isVisible = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showImage() {
        imageAdapter = ImageSliderAdapterUri(getFile, this)
        binding.viewPager.adapter = imageAdapter
        dots = ArrayList()
        binding.dotsIndicator.removeAllViews()
        setIndicator()

        if (getFile.size != 0) {
            binding.pageNumberText.text = "1 / ${getFile.size}"
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.pageNumberText.text = "${position + 1} / ${getFile.size}"
                if (getFile.isNotEmpty()) {
                    for (i in 0 until getFile.size) {
                        if (i == position) dots[i].setTextColor(
                            ContextCompat.getColor(
                                this@DetailQuestionActivity, R.color.green_200
                            )
                        )
                        else dots[i].setTextColor(
                            ContextCompat.getColor(
                                this@DetailQuestionActivity, R.color.lightGrey
                            )
                        )
                    }
                }
                super.onPageSelected(position)
            }
        })
    }

    private fun setIndicator() {
        if (getFile.isNotEmpty()) {
            binding.viewPager.isVisible = true
            binding.dotsIndicator.isVisible = true
            for (i in 0 until getFile.size) {
                dots.add(TextView(this))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dots[i].text = Html.fromHtml("&#9679", Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    dots[i].text = Html.fromHtml("&#9679")
                }
                dots[i].textSize = 15f
                binding.dotsIndicator.addView(dots[i])
            }
        } else {
            binding.viewPager.isVisible = false
            binding.dotsIndicator.isVisible = false
        }
    }

    private fun finishQuestion() {
        viewModel.finishQuestion(questionId).observe(this) { detailQuestion ->
            when (detailQuestion.status) {
                StatusResponses.SUCCESS -> showToast("Forum diskusi telah selesai")
                StatusResponses.ERROR -> isQAFinish(true)
                StatusResponses.EMPTY -> TODO()
            }
        }
    }

    private fun isQAFinish(status: Boolean) {
        if (status) {
            showToast("Tanya jawab telah selesai")
//            binding.btnFinish.text = "Selesai"
        } else {
//            binding.btnFinish.text = "Belum Selesai"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getQuestionData() {

        auth = FirebaseAuth.getInstance()
        val userId = "'" + auth.currentUser?.uid + "'"

        viewModel.getDetailQuestion(questionId).observe(this) { detailQuestion ->
            when (detailQuestion.status) {
                StatusResponses.SUCCESS -> {
                    binding.apply {
                        isLoading(false, "Memuat data...")
                        val tagAdapter = detailQuestion?.body?.tag?.let {
                            TagAdapter(
                                it, this@DetailQuestionActivity, subjectId, lecturerId
                            )
                        }
                        rvTag.setHasFixedSize(true)
                        rvTag.adapter = tagAdapter
                        rvTag.layoutManager = LinearLayoutManager(
                            this@DetailQuestionActivity, LinearLayoutManager.HORIZONTAL, false
                        )


                        val ref =
                            FirebaseStorage.getInstance().reference.child("img/${detailQuestion.body?.askedBy}")
                        try {
                            val localFile = File.createTempFile("tempImage", ".jpeg")
                            ref.getFile(localFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.circleImageView.setImageBitmap(bitmap)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        detailQuestion.body?.let {
                            tvAskedBy.text = it.name
                            tvTitle.text = it.title
                            tvQuestion.text = it.question
                            tvViews.text = "${resources.getString(R.string.views)} : ${it.views} x"
                            tvScore.text = it.totalPoint.toString()
                            val timeAgo = calculateTimeAgo(it.date)
                            tvTimeAgo.text = "${resources.getString(R.string.posted)} : $timeAgo"


                            if (it.status == "selesai") {
                                isQAFinish(true)
                                viewModel.updateQuestionStatus("selesai")
                            } else {
                                isQAFinish(false)
                                viewModel.updateQuestionStatus("belum selesai")
                            }

                            checkVotedOrNot(
                                it.id, userId, it.totalPoint
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

                StatusResponses.EMPTY -> {
                    Toast.makeText(
                        this, resources.getString(R.string.dataEmpty), Toast.LENGTH_SHORT
                    ).show()
                    isLoading(false, "")
                }

                StatusResponses.ERROR -> {
                    Toast.makeText(
                        this, resources.getString(R.string.error), Toast.LENGTH_SHORT
                    ).show()
                    isLoading(false, "")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setQuestionStatusLabel(status: String?) {
        if (status == "selesai") {
            binding.tvStatus.text = "Status: Selesai"
            binding.tvStatus.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.green_500
                )
            )
        } else {
            binding.tvStatus.text = "Status: Belum selesai"
            binding.tvStatus.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.yellow
                )
            )
        }
    }

    private fun checkVotedOrNot(questionId: Int?, userId: String?, score: Int?) {
        var voting = "Insert"

        if (questionId != null && userId != null) {
            viewModel.checkVotedOrNot(questionId, userId).observe(this) { check ->
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
                                    score - (2 * point) -> {
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
                                    score - (2 * point) -> {
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
                                    score!! + point -> {
                                        txtScore -= point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreDown.isSelected = true
                                        point *= -1
                                        voting = "Insert"
                                    }
                                    score + (2 * point) -> {
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
                                    score!! + point -> {
                                        txtScore += point
                                        binding.tvScore.text = txtScore.toString()
                                        binding.btnScoreUp.isSelected = true
                                        point *= 1
                                        voting = "Insert"
                                    }
                                    score + (2 * point) -> {
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
                                score + point -> {
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

                    StatusResponses.ERROR -> Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun checkAccessRightPoint() {
        auth = FirebaseAuth.getInstance()
        viewModel.checkAccessRight(auth.currentUser?.uid.toString()).observe(this) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    point = when (it.body) {
                        "Dosen" -> 5
                        else -> 1
                    }
                }
                StatusResponses.EMPTY -> Toast.makeText(
                    this, "Terjadi kesalahan", Toast.LENGTH_SHORT
                ).show()
                StatusResponses.ERROR -> Toast.makeText(
                    this, "Terjadi kesalahan", Toast.LENGTH_SHORT
                ).show()
            }
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
        showToast(snackBar)
    }

    private fun displayAnswerData(questionId: Int?) {

        if (questionId != null) {
            auth.currentUser?.let {
                viewModel.getAnswer(questionId, it.uid).observe(this) { answer ->
                    when (answer.status) {
                        StatusResponses.SUCCESS -> {
                            binding.rvAnswer.isVisible = true
                            binding.rvAnswer.setHasFixedSize(true)
                            binding.rvAnswer.layoutManager = LinearLayoutManager(this)
                            answer.body?.let {
                                answerList.addAll(it)
                                answerAdapter =
                                    AnswerAdapter(
                                        it,
                                        this,
                                        this,
                                        this,
                                        isPunished,
                                        point,
                                        applicationContext
                                    )
                                binding.rvAnswer.adapter = answerAdapter
                                answerAdapter.notifyDataSetChanged()
                            }
                        }
                        StatusResponses.ERROR -> {
                            Toast.makeText(
                                this, StatusResponses.ERROR.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }

                        StatusResponses.EMPTY -> {
                            Toast.makeText(this, "Belum ada jawaban", Toast.LENGTH_SHORT).show()
                            binding.rvAnswer.isVisible = false
                        }

                    }
                }
            }
        }
    }

    private fun votingQuestion(voting: String, questionId: Int, point: Int) {
        val uid = auth.currentUser?.uid!!
        val response = ScoreResponse(
            questionId, uid, point
        )
        viewModel.votingQuestion(response, voting)
    }

    private fun isLoading(b: Boolean, status: String) {
        binding.apply {
            if (b) {
                progressBarCenter.visibility = View.VISIBLE
                textLoading.visibility = View.VISIBLE
                textLoading.text = status
                layout.visibility = View.GONE
            } else {
                progressBarCenter.visibility = View.GONE
                textLoading.visibility = View.GONE
                layout.visibility = View.VISIBLE
                itemContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun click(question: QuestionTagResponse) {
        TODO("Not yet implemented")
    }

    override fun onImageClicked(userId: String) {
        TODO("Not yet implemented")
    }

    override fun onTagClicked(tag: String, subjectId: Int, lecturerId: String?) {
//        val bundle = Bundle()
////        bundle.putParcelableArray(DetailForumFragment.QUESTION, question)
//        bundle.putInt(HomeFragment.SUBJECTID, subjectId)
//        bundle.putString(HomeFragment.LECTURERID, lecturerId)
//        bundle.putBoolean(HomeFragment.TAGCLICKED.toString(), true)
//        bundle.putString(HomeFragment.TAG, tag)
//        findNavController().navigate(R.id.navigation_home, bundle)
    }

    @SuppressLint("SetTextI18n")
    override fun answerThreeDotsClicked(answer: Answer) {
        _binding2 = LayoutAnswerThreeDotsClickedBinding.inflate(
            layoutInflater
        )
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(binding2.root)
        dialog.show()
        binding2.btnReport.text = "${getString(R.string.report)} ${getString(R.string.answer)}"

        authViewModel.getSession().observe(
            this
        ) {
            binding2.btnVerification.isVisible = it.accessRights == "Lecturer"
        }
        if (answer.verified == 1) binding2.btnVerification.text = "Batalkan verifikasi"
        binding2.btnDelete.text = "${getString(R.string.delete)} ${getString(R.string.answer)}"
        if (answer.answerById != auth.currentUser?.uid) binding2.btnDelete.isVisible = false
        else binding2.btnReport.isVisible = false
        binding2.btnVerification.setOnClickListener {
            dialog.dismiss()

            var isVerified = 0
            if (answer.verified == 0) {
                isVerified = 1
                isLoading(true, "Memverifikasi")
            } else {
                isLoading(true, "Membatalkan verifikasi")
            }
            viewModel.verifiedAnswerHandler(answer.answerId, isVerified).observe(this) {
                if (isVerified == 1 && it.body == "Success") {
                    isLoading(false, "")
                    showToast("Verifikasi berhasil")
                    displayAnswerData(questionId)
                } else if (isVerified == 0 && it.body == "Success") {
                    isLoading(false, "")
                    showToast("Membatalkan verifikasi berhasil")
                    displayAnswerData(questionId)
                } else {
                    isLoading(false, "")
                    showToast("Terjadi kesalahan")
                }
            }
        }

        binding2.btnReport.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(ReportActivity.ID, answer.answerId)
            bundle.putString(ReportActivity.ASKEDBYID, askedById)
            bundle.putString(ReportActivity.LECTURERID, lecturerId)
            bundle.putString(ReportActivity.SUBJECTNAME, subjectName)
            bundle.putString(ReportActivity.ISQUESTION, "false")
            bundle.putInt(ReportActivity.POINT, point)
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        binding2.btnDelete.setOnClickListener {
            deleteQuestionOrAnswerHandler(answer.answerId, false)
            dialog.dismiss()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkBookmark() {
        viewModel.checkBookmark(questionId, userId).observe(this) {
            when (it.status) {
                StatusResponses.SUCCESS -> binding.btnBookmark.isSelected = true
                StatusResponses.EMPTY -> binding.btnBookmark.isSelected = false
                StatusResponses.ERROR -> binding.btnBookmark.isSelected = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.threedots_detailactivity, menu)
        val menuFinish = menu.findItem(R.id.btn_finish)
        val menuAddAnswer = menu.findItem(R.id.btn_add_answer)
        if (questionStatus == "selesai") {
            menuFinish.isVisible = false
            menuAddAnswer.isVisible = false
        }
        if (askedById != auth.currentUser?.uid) menuFinish.isVisible = false
        this.invalidateOptionsMenu()
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isPunished != "Tipe 1") {
            when (item.itemId) {
                R.id.btn_finish -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.finishNote)).setCancelable(false)
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            if (questionStatus != "selesai") questionStatus = "selesai"
                            finishQuestion()
                            this.invalidateOptionsMenu()
                            setQuestionStatusLabel(questionStatus)
                            showToast("Forum diskusi telah dianggap selesai")
                        }.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }
                R.id.btn_add_answer -> {
                    val bundle = Bundle()
                    bundle.putInt(AddAnswerActivity.QUESTIONID, questionId)
                    bundle.putString(AddAnswerActivity.ASKEDBYID, askedById)
                    bundle.putString(AddAnswerActivity.LECTURERID, lecturerId)
                    bundle.putString(AddAnswerActivity.SUBJECTNAME, subjectName)
                    bundle.putInt(AddAnswerActivity.POINT, point)
                    val intent = Intent(this, AddAnswerActivity::class.java)
                    intent.putExtras(bundle)
                    startActivityForResult(intent, ADD_DATA_REQUEST_CODE);
                }
                R.id.btn_more -> {
                    _binding2 = LayoutAnswerThreeDotsClickedBinding.inflate(
                        layoutInflater
                    )
                    val dialog = BottomSheetDialog(this)
                    dialog.setContentView(binding2.root)
                    dialog.show()
                    binding2.btnVerification.isVisible = false
                    binding2.btnDelete.text =
                        "${getString(R.string.delete)} ${getString(R.string.question)}"
                    if (askedById != auth.currentUser?.uid) binding2.btnDelete.isVisible = false
                    else binding2.btnReport.isVisible = false
                    binding2.btnReport.text =
                        "${getString(R.string.report)} ${getString(R.string.question)}"
                    binding2.btnReport.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putInt(ReportActivity.ID, questionId)
                        bundle.putString(ReportActivity.ASKEDBYID, askedById)
                        bundle.putString(ReportActivity.LECTURERID, lecturerId)
                        bundle.putString(ReportActivity.SUBJECTNAME, subjectName)
                        bundle.putString(ReportActivity.ISQUESTION, "true")
                        bundle.putInt(ReportActivity.POINT, point)
                        val intent = Intent(this, ReportActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    binding2.btnDelete.setOnClickListener {
                        deleteQuestionOrAnswerHandler(questionId, true)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteQuestionOrAnswerHandler(deletingId: Int, isQuestion: Boolean) {
        val builder = AlertDialog.Builder(this)
        var message = if (isQuestion) "Hapus pertanyaan anda?"
        else "Hapus jawaban anda?"
        builder.setMessage(message).setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteQAHandler(deletingId, isQuestion).observe(this) {
                    when (it.status) {
                        StatusResponses.SUCCESS -> {
                            if (isQuestion) {
                                showToast("Pertanyaan telah dihapus")
                                val resultIntent = Intent()
                                resultIntent.putExtra("isSuccess", "deleteSuccess");
                                setResult(RESULT_OK, resultIntent);
                                finish()
                            } else {
                                displayAnswerData(questionId)
                                showToast("Jawaban telah dihapus")
                            }
                        }
                        StatusResponses.EMPTY -> showToast("Terjadi kesalahan")
                        StatusResponses.ERROR -> showToast("Terjadi kesalahan")
                    }
                }
            }.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DATA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Data was added in AddDataActivity
            if (data != null) {
                val isQuestion = data.getBooleanExtra("isQuestion", true)
                if (!isQuestion) displayAnswerData(questionId)
                // Do something with the returned value
            }
        }
    }

    override fun voteHandler(answerId: Int, s: String, score: Int) {
        auth.currentUser?.let {
            viewModel.answerVotingHandler(it.uid, score, answerId, s).observe(this) {
                when (it.status) {
                    StatusResponses.SUCCESS -> Toast.makeText(this, it.body, Toast.LENGTH_SHORT)
                        .show()
                    StatusResponses.EMPTY -> Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                    StatusResponses.ERROR -> Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    companion object {
        const val PROBLEMID = "PROBLEMID"
        const val ISPUNISHED = "ISPUNISHED"
        const val ASKEDBYID = "ASKEDBYID"
        const val SUBJECTNAME = "SUBJECTNAME"
        const val LECTURERID = "LECTURERID"
        const val SUBJECTID = "1"
        const val QUESTIONID = "QUESTIONID"
        const val QUESTIONSTATUS = "QUESTIONSTATUS"
        const val ISREPORTED = "ISREPORTED"
    }

    override fun onImageClicked(uri: Uri) {
        val intent = Intent(this, PopUpImageActivity::class.java)
        intent.putExtra("URI", uri.toString())
        startActivity(intent)
    }
}