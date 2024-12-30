package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentUsertypePopUpBinding


class PopUpRegisterAsFragment : DialogFragment() {
    private lateinit var _binding: FragmentUsertypePopUpBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsertypePopUpBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnLecturer.setOnClickListener {
                super.dismiss()
                val fragment = RegisterLecturerFragment()
                val fragmentManager = parentFragmentManager
                fragmentManager.beginTransaction().apply {
                    replace(R.id.frame_container, fragment, RegisterLecturerFragment::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
            }
            btnStudent.setOnClickListener {
                super.dismiss()
                val fragment = RegisterStudentFragment()
                val fragmentManager = parentFragmentManager
                fragmentManager.beginTransaction().apply {
                    replace(R.id.frame_container, fragment, RegisterStudentFragment::class.java.simpleName)
                    addToBackStack(null)
                    commit()
                }
            }
        }

    }
}