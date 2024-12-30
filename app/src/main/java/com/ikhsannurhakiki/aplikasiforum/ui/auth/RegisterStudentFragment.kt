package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Student
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentRegisterStudentBinding


class RegisterStudentFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: Student
    private lateinit var _binding: FragmentRegisterStudentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStudentBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.apply {

            btnLogin.setOnClickListener {
                val fragment = LoginFragment()
                val mFragmentManager = parentFragmentManager
                mFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.frame_container,
                        fragment,
                        LoginFragment::class.java.simpleName
                    )
                    addToBackStack(RegisterStudentFragment::class.java.simpleName)
                    commit()
                }
            }

            btnRegister.setOnClickListener {
                val npm = etNpm.text.toString().trim()
                val firstName = etFirstName.text.toString().trim()
                val lastName = etLastName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                val name = "$firstName $lastName"

                if (npm.isEmpty() || npm.length != 9) {
                    etNpm.error = resources.getString(R.string.npm9char)
                    etNpm.requestFocus()
                } else if (firstName.isEmpty()) {
                    etFirstName.error = resources.getString(R.string.firstNameRequired)
                    etFirstName.requestFocus()
                } else if (lastName.isEmpty()) {
                    etLastName.error = resources.getString(R.string.lastNameRequired)
                    etLastName.requestFocus()
                } else if (email.isEmpty()) {
                    etEmail.error = resources.getString(R.string.emailRequired)
                    etEmail.requestFocus()
                } else if (email.substringAfter('@') != "student.uir.ac.id") {
                    etEmail.error = "Harus menggunakan email student"
                    etEmail.requestFocus()
                } else if (password.isEmpty() || password.length < 6) {
                    etPassword.error = resources.getString(R.string.passwordTooShort)
                    etPassword.requestFocus()
                } else if (confirmPassword.isEmpty()) {
                    etPassword.error = getString(R.string.confPasswordRequired)
                    etPassword.requestFocus()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = resources.getString(R.string.emailNotValid)
                    etEmail.requestFocus()
                } else if (password != confirmPassword) {
                    etConfirmPassword.error = getString(R.string.confpassword)
                    etConfirmPassword.requestFocus()
                } else {
                    user = Student(
                        "",
                        name,
                        email,
                        "",
                        "Student",
                        "",
                        npm,
                        ""
                    )
                    registerUser(name, email, password)
                }
            }
        }

    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) {
                if (it.isSuccessful) {
                    val updateDisplayName =
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    auth.currentUser?.updateProfile(updateDisplayName)

                    val currentUser: FirebaseUser? = auth.currentUser
                    sendVerificationEmail(currentUser)
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) {
                            if (it.isSuccessful) {
                                auth.currentUser?.reload()?.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        if (auth.currentUser?.isEmailVerified == true) {
                                            Toast.makeText(
                                                context,
                                                "Email sudah pernah didaftarkan sebelumnya",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }else{
                                            Toast.makeText(
                                                context,
                                                "Email sudah pernah didaftarkan namun belum terverifikasi",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val currentUser: FirebaseUser? = auth.currentUser
                                            sendVerificationEmail(currentUser)
                                        }
                                    }
                                }
                            }
                        }
                }
            }
    }

    private fun sendVerificationEmail(currentUser: FirebaseUser?) {
        currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    getString(R.string.verificationEmailSent),
                    Toast.LENGTH_SHORT
                ).show()
                val bundle = Bundle()
                bundle.putParcelable(VerifyEmailFragment.EXTRA_USER, user)
                bundle.putString(VerifyEmailFragment.EXTRA_USER_TYPE, "Student")
                val fragment = VerifyEmailFragment()
                val mFragmentManager = parentFragmentManager
                fragment.arguments = bundle
                mFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.frame_container,
                        fragment,
                        VerifyEmailFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                    commit()
                }
//                findNavController().navigate(R.id.fragmentRegisterStudent_to_fragmentVerificationEmail)
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}