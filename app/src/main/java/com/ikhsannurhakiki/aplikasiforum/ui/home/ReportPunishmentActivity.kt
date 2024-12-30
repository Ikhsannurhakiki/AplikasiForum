package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityReportPunishmentBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


class ReportPunishmentActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityReportPunishmentBinding
    private var calculated: Long = 0
    private lateinit var formattedDate: String
    private lateinit var note: String
    private lateinit var type: String
    private lateinit var reportType: String
    private var reportId: Int = 0
    private lateinit var data: Report

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportPunishmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        if (intent.hasExtra("repData")) {
            data = intent.getParcelableExtra("repData")!!
            reportId = data.reportId
            reportType = data.reportType
        }

        val startDate = getCurrentDateTime()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
            binding.datePicker.minDate = todayDate.time
        } else {
            Toast.makeText(this, "Android Oreo ke atas", Toast.LENGTH_SHORT).show()
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.datePicker.setOnDateChangedListener { datePicker, year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val date: Date = calendar.time
                formattedDate = formatDate(date.toString())
                calculated = calculateDaysDifference(startDate, formattedDate)
                if (calculated > 0) {
                    Toast.makeText(this, "${calculated} Hari", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Tidak bisa memilih hari ini", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            var year = binding.datePicker.year
            var month = binding.datePicker.month
            var day = binding.datePicker.dayOfMonth
            binding.datePicker.init(
                year, month, day
            ) { datePicker, year, month, day ->
                Toast.makeText(
                    this,
                    day.toString() + " " + (month + 1) + " " + year,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonSubmit.setOnClickListener {
            if (calculated > 0) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Apakah anda yakin ingin menyimpan data?")
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        note = binding.tvNote.text.toString().trim()
                        val radioGroup = binding.radioPunishType.checkedRadioButtonId
                        when (resources.getResourceEntryName(radioGroup)) {
                            "radio1" -> {
                                type = "Tipe 1"
                            }
                            "radio2" -> {
                                type = "Tipe 2"
                            }
                        }
                        if (type.isEmpty()|| note.isEmpty()){
                            Toast.makeText(this, "Field harus diisi", Toast.LENGTH_SHORT).show()
                        }else{
                            insertPunishment()
                        }

                        Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            } else {
                Toast.makeText(this, "Tidak bisa memilih hari ini", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertPunishment() {
        viewModel.insertPunishment(reportId, type, formattedDate, note, reportType).observe(this){
            when(it.status){
                StatusResponses.SUCCESS -> {
                    Toast.makeText(
                        this, "Data tersimpan", Toast.LENGTH_SHORT
                    ).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("isPunished", true)
                    resultIntent.putExtra("repItem", data)
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                StatusResponses.EMPTY -> Toast.makeText(
                    this, "Data gagal tersimpan", Toast.LENGTH_SHORT
                ).show()
                StatusResponses.ERROR -> Toast.makeText(
                    this, "Data gagal tersimpan", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("GMT+7")
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("GMT+7")
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+7")
        return dateFormat.format(calendar.time)
    }

    fun calculateDaysDifference(startDateStr: String, endDateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+7")
        val startDate: Date = dateFormat.parse(startDateStr) ?: Date()
        val endDate: Date = dateFormat.parse(endDateStr) ?: Date()
        val differenceInMillis: Long = abs(endDate.time - startDate.time)
        return TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    }
}