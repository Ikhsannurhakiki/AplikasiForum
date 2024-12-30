package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.MaterialAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Material
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentMaterialBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class MaterialFragment : Fragment(), GeneralInterface {

    private lateinit var _binding: FragmentMaterialBinding
    private val binding get() = _binding
    private lateinit var materialAdapter: MaterialAdapter
    private lateinit var viewModel: HomeViewModel
    private var lecturerId: String? = null
    private var subjectId: Int = 0
    private var subjectName: String? = null
    private var lecturerName: String? = null
    private var isPunished: String? = null
    private var suppLecturer: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMaterialBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAdd.isVisible = false
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.title = "Materi Perkuliahan"

        arguments?.let {
            subjectId = it.getInt(SUBJECTID)
            subjectName = it.getString(SUBJECTNAME)
            lecturerId = it.getString(LECTURERID)
            lecturerName = it.getString(LECTURERNAME)
            suppLecturer = it.getInt(SUPPLECTURER, 0)
            isPunished = it.getString(ISPUNISHED)
        }
        displayData()
    }

    private fun displayData() {
        viewModel.getAllMaterial(subjectId, suppLecturer)
        viewModel.getMaterialListLiveData.observe(viewLifecycleOwner) { material ->
            when (material.status) {
                StatusResponses.SUCCESS -> {
                    binding.rv.setHasFixedSize(true)
                    binding.rv.layoutManager = LinearLayoutManager(activity)
                    material.body?.let {
                        val adapter = MaterialAdapter(false,this, it)
                        binding.rv.adapter = adapter
                        adapter.notifyDataSetChanged()
                        binding.progressBar.isVisible = false
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

    companion object{
        const val ISPUNISHED = "ISPUNISHED"
        const val SUPPLECTURER = "0"
        const val TAGCLICKED: Boolean = false
        const val LECTURERID = "LECTURERID"
        const val TAG = "TAG"
        const val SUBJECTID = "1"
        const val SUBJECTNAME = "SUBJECT_NAME"
        const val LECTURERNAME = "LECTURER_NAME"
    }

    override fun onListClick(material: Material) {
        val bundle = Bundle()
        bundle.putInt(HomeFragment.SUBJECTID, subjectId)
        bundle.putString(HomeFragment.LECTURERID, lecturerId)
        bundle.putString(HomeFragment.SUBJECTNAME, subjectName)
        bundle.putString(HomeFragment.LECTURERNAME, lecturerName)
        bundle.putInt(HomeFragment.SUPPLECTURER, suppLecturer)
        bundle.putInt(HomeFragment.MATERIALID, material.materialId)
        findNavController().navigate(R.id.fragmentMaterial_to_fragmentHome, bundle)
    }

    override fun onListLongClick(material: Material) {

    }
}