package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityReportBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private var objectReportId: Int? = null
    private lateinit var viewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel
    val pref = UserPreferences.getInstance(dataStore)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectReportId = intent.getIntExtra(ID, 0)
        var isQuestion = intent.getStringExtra(ISQUESTION)
        Log.d("ssssssssss", isQuestion.toString())

        binding.apply {
            buttonReport.setOnClickListener {
                val message = etReportMessage.text.toString().trim()
                authViewModel.getSession().observe(this@ReportActivity) {
                    if (it != null) {
                        isQuestion?.let { it1 ->
                            viewModel.insertReport(message, objectReportId!!, it.userId.toString(),
                                it1
                            ).observe(this@ReportActivity) { result ->
                                when(result.status){
                                    StatusResponses.SUCCESS -> {
                                        Toast.makeText(this@ReportActivity, "Terimakasih atas laporan anda", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    StatusResponses.EMPTY -> Toast.makeText(this@ReportActivity, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                                    StatusResponses.ERROR -> Toast.makeText(this@ReportActivity, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ID = "ID"
        const val ISQUESTION = "ISQUESTION"
        const val POINT = "POINT"
        const val ASKEDBYID = "ASKEDBYID"
        const val QUESTIONID = "QUESTIONID"
        const val SUBJECTNAME = "SUBJECTNAME"
        const val LECTURERID = "LECTURERID"
        const val SUBJECTID = "1"
    }
}