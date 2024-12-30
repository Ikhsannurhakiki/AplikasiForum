package com.ikhsannurhakiki.aplikasiforum.ui.auth

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses.*
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Auth
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Lecturer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Student
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentVerifyEmailBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.main.viewModel
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class VerifyEmailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var _binding: FragmentVerifyEmailBinding
    private val binding get() = _binding
    private var userType: String = ""
    private var subjectList = ArrayList<Subject>()
    private lateinit var token:String
    private lateinit var accessRight: String
    private lateinit var authViewModel: AuthViewModel
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyEmailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val factory = ViewModelFactory.getInstance(requireActivity())
        val activity = activity as AuthActivity
        val pref = UserPreferences.getInstance(activity.dataStore)
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]
        timerCountDown()



        binding.apply {
            btnNext.setOnClickListener {
                auth.currentUser?.reload()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                            auth.currentUser?.let { firebaseUser ->
                                if (arguments != null) {
                                    userType = arguments?.getString(EXTRA_USER_TYPE)!!
                                    Log.d("userType", userType)

                                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                                            return@OnCompleteListener
                                        }
                                        binding.btnResendemail.isEnabled = false
                                        binding.btnNext.isEnabled = false
                                        binding.btnBack.isEnabled = false
                                        token = task.result
                                        if (userType == "Lecturer") {
                                            var user : Lecturer? = null
                                            subjectList =
                                                arguments?.getParcelableArrayList(EXTRA_SUBJECT_LIST)!!
                                            user = arguments?.getParcelable(EXTRA_USER)
                                            user!!.id = firebaseUser.uid
                                            user.deviceToken = token
                                            user.let { it1 -> viewModel.insertLecturer(it1) }
                                            var i = 1
                                            for (a in subjectList) {
                                                viewModel.insertLecturerSubject(a.id, user.id).observe(viewLifecycleOwner){
                                                    when(it.status){
                                                        SUCCESS ->{
                                                            Toast.makeText(context, "${subjectList.size.toString()}  ${i.toString()}", Toast.LENGTH_SHORT).show()
                                                            if(subjectList.size == i){
                                                                intentToHome()
                                                            }else{
                                                                i += 1
                                                            }
                                                        }
                                                        EMPTY -> Toast.makeText(context, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                                                        ERROR -> Toast.makeText(context, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            var user : Student? = null
                                            user = arguments?.getParcelable(EXTRA_USER)
                                            user!!.id = firebaseUser.uid
                                            user.deviceToken = token
                                            user.let { it1 -> viewModel.insertStudent(it1) }
                                            intentToHome()
                                        }
                                    })
//                                    checkAccessRight(auth.currentUser!!.uid)

                                }else{
                                    Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.emailVerificationNotSent),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            btnResendemail.setOnClickListener {
                sendVerificationEmail(auth.currentUser)
                timerCountDown()
            }
            btnBack.setOnClickListener {
                auth.signOut()
                val mFragment = LoginFragment()
                val mFragmentManager = parentFragmentManager
                mFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.frame_container,
                        mFragment,
                        LoginFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                    commit()
                }
            }

        }
    }

    private fun intentToHome(){
        var auth2 = Auth(
            auth.currentUser!!.uid,
            auth.currentUser!!.displayName,
            userType,
            true
        )
        authViewModel.saveSession(auth2)
        Intent(context, MainActivity::class.java).also { intent ->
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun sendVerificationEmail(currentUser: FirebaseUser?) {
        currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    getString(R.string.verificationEmailSent),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun timerCountDown() {
        timer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val second = millisUntilFinished / 1000
                binding.apply {
                    btnResendemail.text = "$second ${activity?.getString(R.string.resendEmail)}"
                    btnResendemail.isClickable = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        btnResendemail.setTextAppearance(R.style.ButtonEmailVerification)
                    }
                }

            }

            override fun onFinish() {
                binding.apply {
                    btnResendemail.text = requireContext().getString(R.string.resendEmail)
                    btnResendemail.isClickable = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        btnResendemail.setTextAppearance(R.style.ButtonGeneral)
                    }
                }
            }
        }
        (timer as CountDownTimer).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun checkAccessRight(uid:String) {
        viewModel.checkAccessRight(uid).observe(viewLifecycleOwner){
            when(it.status){
                SUCCESS -> {
                    accessRight = it.body.toString()
                }
                EMPTY -> Toast.makeText(context, "Terjadi kesalahan",Toast.LENGTH_SHORT).show()
                ERROR -> Toast.makeText(context, "Terjadi kesalahan",Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_USER = "extra_user"
        const val EXTRA_USER_TYPE = "extra_user_type"
        const val EXTRA_SUBJECT_LIST = "extra_subject_list"
    }
}

