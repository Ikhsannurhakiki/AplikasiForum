package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.ReportedListAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentReportedListBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class ReportedListFragment : Fragment(), ReportListInterface {
    private lateinit var _binding: FragmentReportedListBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private var accessRights: String = ""
    private lateinit var viewModel: HomeViewModel
    private val REQUEST_CODE = 10

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.show()
        actionBar?.title = "Daftar pengaduan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val actionBar = requireActivity().actionBar
        actionBar?.title = "Your Fragment Title"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportedListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).setActionBarTitle(resources.getString(R.string.app_name))

        auth = FirebaseAuth.getInstance()

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.items_spinner_array,
            android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position == 0){
                    getAllReportedList("true")
                }else{
                    getAllReportedList("false")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                getAllReportedList("true")
            }

        }
    }

    private fun getAllReportedList(isQuestion : String) {
        auth.currentUser?.let {
            viewModel.getAllReportedList(it.uid, isQuestion)
                .observe(viewLifecycleOwner) { result ->
                when (result.status) {
                    StatusResponses.SUCCESS -> {
                        binding.rvReportedList.setHasFixedSize(true)
                        binding.rvReportedList.layoutManager = LinearLayoutManager(activity)
                        result.body?.let {
                            val adapter = ReportedListAdapter(this, it)
                            binding.rvReportedList.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                    }
                    StatusResponses.ERROR -> {
//                        Toast.makeText(
//                            context, "Terjadi kesalahan", Toast.LENGTH_SHORT
//                        ).show()
                    }

                    StatusResponses.EMPTY -> {
                        Toast.makeText(context, "Data tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onClick(item: Report) {
        val intent = Intent(activity, ReportHandlerActivity::class.java)
        intent.putExtra("repItem", item)
        Log.d("rpid", item.reportId.toString())
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                val isCancel = data.getBooleanExtra("isCancel",false)
                val isQuestion = data.getBooleanExtra("isQuestion",false)
                if (isCancel){
                    getAllReportedList(isQuestion.toString())

                }
            }
        }
    }

}