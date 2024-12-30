package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.annotation.Nullable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityReportHandlerBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.io.File

class ReportHandlerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportHandlerBinding
    private var reportId: Int = 0
    private lateinit var data: Report
    private lateinit var viewModel: HomeViewModel
    private lateinit var auth: FirebaseAuth
    private val REQUEST_CODE = 101
    private lateinit var cause : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportHandlerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Menanggapi pengaduan"

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        auth = FirebaseAuth.getInstance()

        if (intent.hasExtra("repItem")) {
            data = intent.getParcelableExtra("repItem")!!
            if (intent.hasExtra("isConfirm")){
                data.status = "Diterima"
            }
            if (intent.hasExtra("isDecline")){
                data.status = "Ditolak"
                cause = intent.getStringExtra("cause").toString()
                Toast.makeText(this, cause, Toast.LENGTH_SHORT).show()
                binding.reasonRefuse.text = cause
            }
            binding.reportedUsername.text = data.reportedName
            binding.reporterUsername.text = data.reporterName
            showReportedImage(data.reportedId)
            showReporterImage(data.reporterId)
//            viewModel.imageLiveData.observe(this) { bitmap ->
//                Glide.with(this)
//                    .load(bitmap)
//                    .placeholder(R.drawable.img)
//                    .into(binding.ciAvatarReporter)
//            }

            binding.tvStatus.text = data.status
            when (data.status) {
                "Diproses" -> {
                    binding.tvStatus.setBackgroundResource(R.color.yellow)
                    binding.llRefuse.isVisible = false
                    binding.reason.text = data.note
                    showButton(true)
                }
                "Diterima" -> {
                    binding.tvStatus.setBackgroundResource(R.color.green_500)
                    binding.llRefuse.isVisible = false
                    binding.reason.text = data.note
                    showButton(false)
                    binding.btnCancel.isVisible = true
                }
                "Ditolak" -> {
                    var txt1 = data.note.substringAfter("//Refuse//")
                    binding.llRefuse.isVisible = true
                    binding.tvStatus.setBackgroundResource(R.color.red)
                    if (!intent.hasExtra("isDecline")){
                        binding.reasonRefuse.text = txt1
                    }
                    var txt2 = data.note.substringBefore("//Refuse//")
                    binding.reason.text = txt2
                    binding.btnCancel.isVisible = true
                    showButton(false)
                }
            }

            binding.reportType.text = data.reportType
            reportId = data.reportId
            binding.btnSee.text = "Lihat ${data.reportType}"
        }



        binding.btnHandler.setOnClickListener {
            val intent = Intent(this, ReportPunishmentActivity::class.java)
            intent.putExtra("repData", data)
            startActivityForResult(intent, REQUEST_CODE)
        }

        binding.btnDecline.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("reportData", data)
            val popUpDecline = DeclineReportPopUpFragment()
            popUpDecline.arguments = bundle
            popUpDecline.show(supportFragmentManager, "showDeclinePopUp")
        }

        binding.btnCancel.setOnClickListener {
            var status = data.status
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah anda yakin ingin membatalkan keputusan anda?")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    if (status == "Diterima") {
                        deleteDecision(reportId, status, "")
                    } else if (status == "Ditolak") {
                        deleteDecision(reportId, status, data.note)
                    }
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.btnSee.setOnClickListener {
            viewModel.getReportedData(reportId, data.reportType)
                .observe(this) { result ->
                    when (result.status) {
                        StatusResponses.SUCCESS -> {
                            val bundle = Bundle()
                            result.body?.let {
                                bundle.putInt(DetailQuestionActivity.SUBJECTID, it.subjectId)
                                bundle.putString(
                                    DetailQuestionActivity.LECTURERID,
                                    it.lecturerId
                                )
                                if (data.reportType == "Pertanyaan") {
                                    bundle.putInt(
                                        DetailQuestionActivity.QUESTIONID,
                                        data.problemId.toInt()
                                    )
                                } else {
                                    bundle.putInt(
                                        DetailQuestionActivity.QUESTIONID,
                                        it.questionId
                                    )
                                    bundle.putInt(
                                        DetailQuestionActivity.PROBLEMID,
                                        data.problemId.toInt()
                                    )
                                }

                                bundle.putString(DetailQuestionActivity.ASKEDBYID, it.askedById)
                                bundle.putString(DetailQuestionActivity.SUBJECTNAME, it.subject)
                                bundle.putString(
                                    DetailQuestionActivity.QUESTIONSTATUS,
                                    it.status
                                )
                                bundle.putString(
                                    DetailQuestionActivity.ISREPORTED,
                                    data.reportType
                                )
                            }
                            val intent = Intent(this, DetailQuestionActivity::class.java)
                            intent.putExtras(bundle)
                            intent.putExtra("repData", data)
                            startActivity(intent)
                        }
                        StatusResponses.EMPTY -> Toast.makeText(
                            this,
                            "Teradi kesalahan",
                            Toast.LENGTH_SHORT
                        ).show()
                        StatusResponses.ERROR -> Toast.makeText(
                            this,
                            result.msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun deleteDecision(reportId: Int, status: String, note: String) {
        viewModel.deleteReportDecision(reportId, status, note)
            .observe(this) { result ->
                when (result.status) {
                    StatusResponses.SUCCESS -> {
                        Toast.makeText(
                            this, "Pembatalan berhasil", Toast.LENGTH_SHORT
                        ).show()
                        val resultIntent = Intent()
                        resultIntent.putExtra("isCancel", true);
                        var isQuestion = false
                        if(data.reportType == "Pertanyaan"){
                            isQuestion = true
                        }
                        resultIntent.putExtra("isQuestion", isQuestion);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                    StatusResponses.EMPTY -> Toast.makeText(
                        this,
                        "Terjadi Kesalahan",
                        Toast.LENGTH_SHORT
                    ).show()

                    StatusResponses.ERROR -> Toast.makeText(
                        this,
                        "Koneksi error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showButton(show: Boolean) {
        binding.btnSee.isVisible = show
        binding.btnHandler.isVisible = show
        binding.btnDecline.isVisible = show

    }

    private fun showReporterImage(reporterId: String) {
        val ref = FirebaseStorage.getInstance().reference.child("img/${reporterId}")
        val localFile = File.createTempFile(reporterId, ".jpeg")
        ref.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Glide.with(applicationContext)
                    .load(bitmap)
                    .placeholder(R.drawable.img)
                    .into(binding.ciAvatarReporter)

            }
    }

    private fun showReportedImage(reportedId: String) {
        val ref = FirebaseStorage.getInstance().reference.child("img/${reportedId}")
        val localFile = File.createTempFile(reportedId, ".jpeg")
        ref.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Glide.with(applicationContext)
                    .load(bitmap)
                    .placeholder(R.drawable.img)
                    .into(binding.ciAvatarReported)

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                val isPunished = data.getBooleanExtra("isPunished",false)
                val repItem = data.getParcelableExtra<Report>("repItem")
                val isDecline = data.getBooleanExtra("isDecline",false)
                if (isPunished){
                    val intent = Intent(this, ReportHandlerActivity::class.java)
                    intent.putExtra("repItem", repItem)
                    intent.putExtra("isConfirm", true)
                    startActivityForResult(intent, REQUEST_CODE)
                }
                if (isDecline){
//                    finish()
//                    Toast.makeText(this , "bbbbbbbbb", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this, ReportHandlerActivity::class.java)
//                    intent.putExtra("repItem", repItem)
//                    intent.putExtra("isDecline", true)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
                }
            }
        }
    }
}