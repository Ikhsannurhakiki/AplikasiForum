package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.SubjectAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentSubjectBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class SubjectFragment : Fragment(), SubjectInterface {
    private lateinit var _binding: FragmentSubjectBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private var accessRights: String = ""
    private lateinit var viewModel: HomeViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).setActionBarTitle(resources.getString(R.string.app_name))

        auth = FirebaseAuth.getInstance()
        viewModel.checkAccessRight(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { accessRight ->
                when (accessRight.status) {
                    StatusResponses.SUCCESS -> {
                        when (accessRight.body) {
                            "Dosen" -> getLecturerSubject()
                            else -> getSubject()
                        }
                    }
                    StatusResponses.EMPTY -> {
                        Snackbar.make(requireView(), "EMPTY", Snackbar.LENGTH_SHORT)
                    }

                    StatusResponses.ERROR -> {
                        Toast.makeText(
                            context,
                            "Koneksi error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun getSubject() {
        viewModel.getSubject()
        viewModel.subjectLiveData.observe(viewLifecycleOwner) { subject ->
            when (subject.status) {
                StatusResponses.SUCCESS -> {
                    binding.rvSubject.setHasFixedSize(true)
                    binding.rvSubject.layoutManager = LinearLayoutManager(context)
                    subject.body?.let {
                        val adapter = SubjectAdapter(this, it, accessRights)
                        binding.rvSubject.adapter = adapter
                        adapter.notifyDataSetChanged()
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

    private fun getLecturerSubject() {
        val accessRights = "Dosen"
        viewModel.getLecturerSubject(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { subject ->
                when (subject.status) {
                    StatusResponses.SUCCESS -> {
                        binding.rvSubject.setHasFixedSize(true)
                        binding.rvSubject.layoutManager = LinearLayoutManager(activity)
                        subject.body?.let {
                            val adapter = SubjectAdapter(this, it, accessRights)
                            binding.rvSubject.adapter = adapter
                            adapter.notifyDataSetChanged()
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

    override fun onClick(subjectId: Int, subjectName: String, accessRight: String) {
        if (accessRight == "Dosen") {
            auth = FirebaseAuth.getInstance()

            viewModel.getLecturerBySubject(subjectId)
            auth.currentUser?.let {
                viewModel.getSuppLecturerId(it.uid, subjectId).observe(viewLifecycleOwner){ result->
                    when(result.status){
                        StatusResponses.SUCCESS ->{
                            val bundle = Bundle()
                            bundle.putInt(HomeFragment.SUBJECTID, subjectId)
                            bundle.putString(HomeFragment.LECTURERID, auth.currentUser?.uid)
                            bundle.putString(HomeFragment.SUBJECTNAME, subjectName)
                            result.body?.let { suppLecturer ->
                                bundle.putInt(HomeFragment.SUPPLECTURER, suppLecturer) }
                            findNavController().navigate(R.id.fragmentPopUp_to_fragmentMaterial, bundle)
                        }
                    }
                }
            }
        } else {
            val bundle = Bundle()
            bundle.putInt(LecturerPopUpFragment.SUBJECTID, subjectId)
            bundle.putString(LecturerPopUpFragment.SUBJECTNAME, subjectName)
            Log.d("subject", subjectId.toString())
            val showPopUp = LecturerPopUpFragment()
            showPopUp.arguments = bundle
            showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "ShowPopUp")
        }
    }

}