package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.content.ContentValues
import android.content.Intent
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Auth
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentLoginBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var _binding: FragmentLoginBinding
    private val binding get() = _binding
    private lateinit var token: String
    private lateinit var authViewModel: AuthViewModel
    private lateinit var viewModel: HomeViewModel
    private lateinit var accessRight: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity as AuthActivity
        val pref = UserPreferences.getInstance(activity.dataStore)
        val factory = ViewModelFactory.getInstance(requireActivity())
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        binding.apply {
            btnRegister.setOnClickListener {
                val showPopUp = PopUpRegisterAsFragment()
                showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "ShowPopUp")
            }

            btnLogin.setOnClickListener {
                binding.apply {
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    if (email.isEmpty()) {
                        etEmail.error = resources.getString(R.string.emailRequired)
                        etEmail.requestFocus()
                    } else if (password.isEmpty()) {
                        etPassword.error = resources.getString(R.string.passwordRequired)
                        etPassword.requestFocus()
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) {
                                if (it.isSuccessful) {
                                    auth.currentUser?.reload()?.addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            if (auth.currentUser?.isEmailVerified == true) {
                                                checkUser()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Email sudah pernah didaftarkan namun belum terverifikasi",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Toast.makeText(
                                                    context,
                                                    "Lakukan pendaftaran ulang dan verifikasi email anda",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Periksa kembali email dan sandi anda!", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
            btnForgetPass.setOnClickListener {
                binding.apply {
                    val email = etEmail.text.toString().trim()
                    if (email.isEmpty()) {
                        etEmail.error = resources.getString(R.string.emailRequired)
                        etEmail.requestFocus()
                    } else {
                        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                if (signInMethods != null && signInMethods.isNotEmpty()) {
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Email reset sandi telah terkirim",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Email reset sandi gagal terkirim",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Email belum terdaftar, periksa kembali email anda",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                task.exception?.let {
                                    Log.e("AuthCheck", "Error checking if email is registered", it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    private fun checkUser() {
        if (auth.currentUser != null) {
            auth.currentUser!!.reload()
            viewModel.checkAccessRight(auth.currentUser!!.uid)
                .observe(viewLifecycleOwner) {
                    when (it.status) {
                        StatusResponses.SUCCESS -> {
                            accessRight = if (it.body == "Dosen") {
                                "Lecturer"
                            } else {
                                "Student"
                            }
                            if (auth.currentUser!!.isEmailVerified) {
                                updateDeviceToken(auth.currentUser!!.uid)
                                val auth2 = Auth(
                                    auth.currentUser!!.uid,
                                    auth.currentUser!!.displayName,
                                    accessRight,
                                    true
                                )
                                authViewModel.saveSession(auth2)
                                Intent(context, MainActivity::class.java).also { intent ->
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            } else {
                                val mFragment = VerifyEmailFragment()
                                val mFragmentManager = parentFragmentManager
                                mFragmentManager.beginTransaction().apply {
                                    replace(
                                        R.id.frame_container,
                                        mFragment,
                                        VerifyEmailFragment::class.java.simpleName
                                    )
                                    addToBackStack(null)
                                    commit()
                                }
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
    }

    private fun checkAccessRight(uid: String) {
        viewModel.checkAccessRight(uid)
            .observe(viewLifecycleOwner) {
                when (it.status) {
                    StatusResponses.SUCCESS -> {
                        Log.d("accesss", it.body.toString())
                        accessRight = if (it.body == "Dosen") {
                            "Lecturer"
                        } else {
                            "Student"
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

    private fun updateDeviceToken(uid: String) {
        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    ContentValues.TAG,
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@OnCompleteListener
            }
            token = task.result
            viewModel.updateUserDeviceToken(uid, token)
        })
    }

    fun navControllerNavigate(accessRight: String) {
        if (accessRight == "Lecturer") {
            findNavController().navigate(R.id.fragmentLogin_to_fragmentRegisterLecturer)
        } else {
            findNavController().navigate(R.id.fragmentLogin_to_fragmentRegisterStudent)
        }
    }
}