package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.LecturerAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentLecturerPopUpBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class LecturerPopUpFragment : DialogFragment(), LecturerPopUpInterface {

    private lateinit var _binding: FragmentLecturerPopUpBinding
    private val binding get() = _binding
    private var subjectId: Int = 0
    private var subjectName: String? = null
    private lateinit var viewModel: HomeViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLecturerPopUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.cvItemForum.visibility = View.INVISIBLE
        arguments?.let {
            subjectId = it.getInt(SUBJECTID)
            subjectName = it.getString(SUBJECTNAME)
        }

        viewModel.getLecturerBySubject(subjectId)
        viewModel.getLecturerBySubjectLiveData.observe(viewLifecycleOwner) { lecturer ->
            when (lecturer.status) {
                StatusResponses.SUCCESS -> {
                    binding.cvItemForum.visibility = View.VISIBLE
                    binding.rvSubject.setHasFixedSize(true)
                    binding.rvSubject.layoutManager = LinearLayoutManager(activity)
                    lecturer.body?.let {
                        val adapter = LecturerAdapter(this, it, subjectId!!.toInt())
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

    override fun onLecturerClick(
        lecturerId: String,
        lecturerName: String,
        subjectId: Int,
        suppLecturer: Int
    ) {

        val userId = auth.currentUser!!.uid
        viewModel.checkPunishment(suppLecturer, userId).observe(viewLifecycleOwner) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    var isType2 = false
                    var isType1= false
                    val currentDate = Date()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    for (i in 0 until it.body!!.size) {
                        val punishmentDateTime = it.body?.get(i)?.date?.let { it1 ->
                            dateFormat.parse(
                                it1
                            )
                        }
                        if (punishmentDateTime != null) {
                            if (punishmentDateTime.before(currentDate)){
                            }else{
                                if (it.body?.get(i)?.type.toString() == "Tipe 2") {
                                    isType2 = true
                                }
                                if (it.body?.get(i)?.type.toString() == "Tipe 1") {
                                    isType1 = true
                                }
                            }
                        }
                    }
                    if (isType2) {
                        Toast.makeText(
                            context,
                            "Anda tidak diizinkan masuk ke forum, Silahkan hubungi dosen bersangkutan!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else if (isType1){
                        val bundle = Bundle()
                        bundle.putInt(HomeFragment.SUBJECTID, subjectId)
                        bundle.putString(HomeFragment.LECTURERID, lecturerId)
                        bundle.putString(HomeFragment.SUBJECTNAME, subjectName)
                        bundle.putString(HomeFragment.LECTURERNAME, lecturerName)
                        bundle.putInt(HomeFragment.SUPPLECTURER, suppLecturer)
                        bundle.putString(HomeFragment.ISPUNISHED, "Tipe 1")
                        super.dismiss()
                        findNavController().navigate(R.id.fragmentPopUp_to_fragmentMaterial, bundle)
                    }else{
                        val bundle = Bundle()
                        bundle.putInt(HomeFragment.SUBJECTID, subjectId)
                        bundle.putString(HomeFragment.LECTURERID, lecturerId)
                        bundle.putString(HomeFragment.SUBJECTNAME, subjectName)
                        bundle.putString(HomeFragment.LECTURERNAME, lecturerName)
                        bundle.putInt(HomeFragment.SUPPLECTURER, suppLecturer)
                        super.dismiss()
                        findNavController().navigate(R.id.fragmentPopUp_to_fragmentMaterial, bundle)
                    }
                }
                StatusResponses.EMPTY -> {
                    val bundle = Bundle()
                    bundle.putInt(MaterialFragment.SUBJECTID, subjectId)
                    bundle.putString(MaterialFragment.LECTURERID, lecturerId)
                    bundle.putString(MaterialFragment.SUBJECTNAME, subjectName)
                    bundle.putString(MaterialFragment.LECTURERNAME, lecturerName)
                    bundle.putInt(MaterialFragment.SUPPLECTURER, suppLecturer)
                    super.dismiss()
                    findNavController().navigate(R.id.fragmentPopUp_to_fragmentMaterial, bundle)
                }
                StatusResponses.ERROR -> {
                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDetailsButtonClick(lecturerId: String) {
        Toast.makeText(context, lecturerId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SUBJECTID = "1"
        const val SUBJECTNAME = "SUBJECT_NAME"
    }

}