package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.SubjectCheckBoxAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Lecturer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.databinding.DialogAlertBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentRegisterLecturerBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class RegisterLecturerFragment : Fragment(), RegisterLecturerInterface {

    private lateinit var _binding: FragmentRegisterLecturerBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private var subjectList = ArrayList<Subject>()
    private var emptyField: Boolean = true
    private lateinit var user: Lecturer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterLecturerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.getSubject()
        viewModel.subjectLiveData.observe(viewLifecycleOwner) { subject ->
            when (subject.status) {
                StatusResponses.SUCCESS -> {
                    binding.rvSubject.setHasFixedSize(true)
                    binding.rvSubject.layoutManager = LinearLayoutManager(activity)
                    subject.body?.let {
                        val adapter = SubjectCheckBoxAdapter(this, it, null)
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
                    addToBackStack(RegisterLecturerFragment::class.java.simpleName)
                    commit()
                }
            }

            btnRegister.setOnClickListener {
                val lecturerName = etName.text.toString().trim()
                val nip = etNip.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val info = etInfo.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                val confirmPass = etConfirmPassword.text.toString().trim()
                emptyField = true
                //check SUBJCT
                if (subjectList.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Profil -> Kelola matkul diampu (untuk menegelola matkul diampu)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                //Check NAME
                when {
                    lecturerName.isEmpty() -> {
                        textInputLayout1.error = getString(R.string.name_empty)
                        textInputLayout1.requestFocus()
                        emptyField = true
                    }
                    else -> {
                        //CheCk NIDN
                        textInputLayout1.isErrorEnabled = false
                        when {
                            nip.isEmpty() -> {
                                textInputLayout6.error = getString(R.string.emptyNIDN)
                                textInputLayout6.requestFocus()
                                emptyField = true
                            }
                            else -> {
                                textInputLayout6.isErrorEnabled = false
                                //Check EMAIL
                                when {
                                    email.isEmpty() -> {
                                        textInputLayout2.error = getString(R.string.emailRequired)
                                        textInputLayout2.requestFocus()
                                        emptyField = true
                                    }
                                    else -> {
                                        when {
                                            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                                textInputLayout2.error =
                                                    resources.getString(R.string.emailNotValid)
                                                textInputLayout2.requestFocus()
                                                emptyField = true
                                            }
                                            else -> {
                                                when {
                                                    email.substringAfter('@') != "eng.uir.ac.id" -> {
                                                        textInputLayout2.error =
                                                            "Harus menggunakan email dosen"
                                                        textInputLayout2.requestFocus()
                                                        emptyField = true
                                                    }
                                                    else -> {
                                                        textInputLayout2.isErrorEnabled = false
                                                        //check INFO
                                                        when {
                                                            info.isEmpty() -> {
                                                                textInputLayout3.error =
                                                                    getString(R.string.infoRequired)
                                                                textInputLayout3.requestFocus()
                                                                emptyField = true
                                                            }
                                                            else -> {
                                                                textInputLayout3.isErrorEnabled =
                                                                    false
                                                                //check PASSWORD
                                                                when {
                                                                    pass.length < 6 -> {
                                                                        textInputLayout4.error =
                                                                            getString(R.string.passwordTooShort)
                                                                        textInputLayout4.requestFocus()
                                                                        emptyField = true
                                                                    }
                                                                    else -> {
                                                                        textInputLayout4.isErrorEnabled =
                                                                            false
                                                                        //check CONFIRMATION PASS
                                                                        when {
                                                                            confirmPass.length < 6 -> {
                                                                                textInputLayout5.error =
                                                                                    getString(R.string.passwordTooShort)
                                                                                textInputLayout5.requestFocus()
                                                                                emptyField = true
                                                                            }
                                                                            else -> {
                                                                                when {
                                                                                    pass != confirmPass -> {
                                                                                        textInputLayout5.error =
                                                                                            getString(
                                                                                                R.string.confpassword
                                                                                            )
                                                                                        textInputLayout5.requestFocus()
                                                                                        emptyField =
                                                                                            true
                                                                                    }
                                                                                    else -> {
                                                                                        textInputLayout5.isErrorEnabled =
                                                                                            false
                                                                                        emptyField =
                                                                                            false
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                user = Lecturer(
                    "",
                    lecturerName,
                    email,
                    info,
                    "Lecturer",
                    "-",
                    nip,
                    0
                )

                when (emptyField) {
                    true -> Log.d("emptyField", emptyField.toString())
                    false -> registerLecturer(lecturerName, email, pass)
                }
            }
        }
    }

    override fun checkBoxChecked(subject: Subject, checked: Boolean) {
        Toast.makeText(context, subject.id.toString(), Toast.LENGTH_SHORT).show()
        when (checked) {
            true -> subjectList.add(subject)
            false -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                subjectList.removeIf {
                    it.id == subject.id
                }
            }
        }
    }

    private fun registerLecturer(
        lecturerName: String,
        email: String,
        pass: String,
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(requireActivity()) {
                if (it.isSuccessful) {
                    val updateDisplayName =
                        UserProfileChangeRequest.Builder().setDisplayName(lecturerName).build()
                    auth.currentUser?.updateProfile(updateDisplayName)

                    val currentUser: FirebaseUser? = auth.currentUser
                    sendVerificationEmail(currentUser)
                } else {
                    auth.signInWithEmailAndPassword(email, pass)
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
                                        } else {
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
                            } else {
                                showNote()
                            }
                        }
                }
            }
    }

    private fun showNote() {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.ivIcon.setImageResource(R.drawable.ic_outline_info_24)
        dialogBinding.positiveButton.isVisible = false
        dialogBinding.positiveButton.isEnabled = false
        dialogBinding.negativeButton.text = "Tutup"
        dialogBinding.negativeButton.setTextColor(resources.getColor(R.color.black))
        dialogBinding.negativeButton.isVisible = true
        dialogBinding.tvText.text =
            "Periksa kembali email dan sandi anda, Karena email sudah pernah di daftarkan maka" +
                    " pastikan sandi anda sama dengan yang pernah anda daftarkan, Jika lupa, maka reset sandi dengan menginputkan email kemudian menekan (Lupa sandi) di halaman masuk (Login).\n\n" +
                    "Setelah reset selesai, daftarkan kembali (Register) akun anda sesuai dengan  sandi yang telah direset "
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
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

                val mFragmentManager = parentFragmentManager
                val fragment = VerifyEmailFragment()
                val bundle = Bundle()

                bundle.putParcelable(VerifyEmailFragment.EXTRA_USER, user)
                bundle.putString(VerifyEmailFragment.EXTRA_USER_TYPE, "Lecturer")
                bundle.putParcelableArrayList(VerifyEmailFragment.EXTRA_SUBJECT_LIST, subjectList)
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
//               findNavController().navigate(R.id.fragmentRegisterLecturer_to_fragmentVerifyEmail)
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}