package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentDeclineReportPopUpBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class DeclineReportPopUpFragment : BottomSheetDialogFragment() {
    private lateinit var _binding: FragmentDeclineReportPopUpBinding
    private val binding get() = _binding
    private lateinit var viewModel: HomeViewModel
    private var reportId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val data = arguments?.getParcelable<Report>("reportData")
        if (data != null) {
            reportId = data.reportId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeclineReportPopUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable<Report>("reportData")

        binding.btnSubmit.setOnClickListener {
            val txtReason = binding.etReason.text.toString().trim()
            if (txtReason.isEmpty()) {
                Toast.makeText(context, "Seluruh inputtan harus diisi", Toast.LENGTH_SHORT).show()
            }else{
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Simpan penolakan pengaduan? (Setelah disimpan tidak dapat diubah) ")
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.declineReport(reportId, txtReason).observe(viewLifecycleOwner) {
                            when (it.status) {
                                StatusResponses.SUCCESS -> {
                                    Toast.makeText(context, it.body, Toast.LENGTH_SHORT).show()
                                    dialog?.dismiss()
                                    val intent =
                                        Intent(requireActivity(), ReportedListFragment::class.java)
                                    intent.putExtra("isDecline", true)
                                    intent.putExtra("repItem", data)
                                    intent.putExtra("cause", txtReason)
                                    startActivity(intent)
                                }
                                StatusResponses.EMPTY -> Toast.makeText(
                                    context,
                                    it.msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                                StatusResponses.ERROR -> Toast.makeText(
                                    context,
                                    it.msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }
    }
}