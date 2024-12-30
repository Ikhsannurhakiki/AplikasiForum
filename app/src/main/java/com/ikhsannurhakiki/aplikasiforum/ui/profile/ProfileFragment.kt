package com.ikhsannurhakiki.aplikasiforum.ui.profile

import android.content.Intent
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentProfileBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ManageSubjectActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.PreferenceActivity
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = resources.getString(R.string.profile)
        actionBar?.show()
        authViewModel.getSession().observe(viewLifecycleOwner) {
            if (it.accessRights == "Lecturer") {
                binding.btnMyQuestion.isVisible = false
                getDetailLecturer()
            } else {
                binding.btnReportList.isVisible = false
                binding.btnManageSubject.isVisible = false
                binding.btnManageSuppSubject.isVisible = false
                getDetailStudent()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val activity = activity as MainActivity
        val pref = UserPreferences.getInstance(activity.dataStore)
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.apply {

            email.text = auth.currentUser?.email

            profile.setOnClickListener {
                findNavController().navigate(R.id.fragmentProfile_to_fragmentEditProfile)
            }

            btnEditProfile.setOnClickListener {
                findNavController().navigate(R.id.fragmentProfile_to_fragmentEditProfile)

            }

            btnMyQuestion.setOnClickListener {
                findNavController().navigate(R.id.fragmentProfile_to_fragmentMyQuestion)
            }

            btnMyBookmark.setOnClickListener {
                findNavController().navigate(R.id.fragmentProfile_to_fragmentBookmark)
            }

            btnReportList.setOnClickListener {
                findNavController().navigate(R.id.fragmentProfile_to_fragmentReportedList)
            }

            btnSetting.setOnClickListener {
                startActivity(Intent(context, PreferenceActivity::class.java))
            }

            btnManageSubject.setOnClickListener{
                startActivity(Intent(context, ManageSubjectActivity::class.java))
            }

            btnManageSuppSubject.setOnClickListener{
                findNavController().navigate(R.id.fragmentProfile_to_fragmentManageSuppLecturer)
            }
        }
    }

    private fun getDetailStudent() {
        viewModel.getDetailStudent(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { detailUser ->
                when (detailUser.status) {
                    StatusResponses.SUCCESS -> {
                        detailUser.body?.let {
                            binding.apply {
                                username.text = it.name
                                checkImage()
                                binding.llLayout.isVisible = true
                            }
                        }
                    }
                    StatusResponses.EMPTY -> Toast.makeText(
                        context,
                        "Terjadi Kesalahanaaa",
                        Toast.LENGTH_SHORT
                    ).show()

                    StatusResponses.ERROR -> Toast.makeText(
                        context,
                        "Koneksi error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getDetailLecturer() {
        viewModel.getDetailLecturer(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { detailUser ->
                when (detailUser.status) {
                    StatusResponses.SUCCESS -> {
                        detailUser.body?.let {
                            binding.apply {
                                username.text = it.name
                                checkImage()
                                binding.llLayout.isVisible = true
                            }
                        }
                    }
                    StatusResponses.EMPTY -> Toast.makeText(
                        context,
                        "Terjadi kesalahan",
                        Toast.LENGTH_SHORT
                    ).show()

                    StatusResponses.ERROR -> Toast.makeText(
                        context,
                        "Koneksi error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkImage() {
        auth = FirebaseAuth.getInstance()
        auth.currentUser?.uid.let {
            if (it != null) {
                viewModel.checkProfileImage(it)
                viewModel.imageLiveData.observe(viewLifecycleOwner) { bitmap ->
                    Glide.with(requireContext())
                        .load(bitmap)
                        .placeholder(R.drawable.img)
                        .into(binding.ciAvatar)
                }
            }
        }
    }
}