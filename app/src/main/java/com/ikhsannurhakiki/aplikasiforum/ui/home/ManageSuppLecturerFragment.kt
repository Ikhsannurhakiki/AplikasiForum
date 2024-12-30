package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.SubjectCheckBoxAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.SuppSubject
import com.ikhsannurhakiki.aplikasiforum.databinding.DialogAlertBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentManageSuppLecturerBinding
import com.ikhsannurhakiki.aplikasiforum.ui.auth.RegisterLecturerInterface
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class ManageSuppLecturerFragment : Fragment(), RegisterLecturerInterface {

    private var subjectList = ArrayList<Subject>()
    private var newSubjectList = ArrayList<Subject>()
    private var oldSubjectList = ArrayList<Subject>()
    private lateinit var _binding: FragmentManageSuppLecturerBinding
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentManageSuppLecturerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        getLecturerSubject()
        viewModel.getSubject()
        viewModel.subjectLiveData.observe(viewLifecycleOwner) { subject ->
            when (subject.status) {
                StatusResponses.SUCCESS -> {
                    binding.rvSubject.setHasFixedSize(true)
                    binding.rvSubject.layoutManager = LinearLayoutManager(activity)
                    subject.body?.let {
                        val adapter = SubjectCheckBoxAdapter(this, it, oldSubjectList)
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
        binding.btnSave.setOnClickListener {
            if (newSubjectList.isNotEmpty()) {
                val deletedElements = oldSubjectList.filterNot { oldSubject ->
                    newSubjectList.any { newSubject -> newSubject.id == oldSubject.id }
                }
                val addedElements = newSubjectList.filterNot { oldSubject ->
                    oldSubjectList.any { newSubject -> newSubject.id == oldSubject.id }
                }
                var a = "Menghapus "
                var b = "Menambahkan "
                var aIsChange = true
                var bIsChange = true
                if(deletedElements.isNotEmpty()){
                    deletedElements.forEach {
                        a += " " + it.subject +","
                    }
                    a+= "\n"
                }else{
                    a += " - \n"
                    aIsChange = false
                }
                if (addedElements.isNotEmpty()){
                    addedElements.forEach {
                        b += " " + it.subject +","
                    }
                }else{
                    b += " - \n"
                    bIsChange = false
                }
                if (aIsChange || bIsChange) {
                    val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
                    val builder = AlertDialog.Builder(requireContext())
                        .setView(dialogBinding.root)
                    val dialogAlert = builder.show()
                    dialogBinding.positiveButton.text = "Lanjut"
                    dialogBinding.negativeButton.text = "Batal"
                    dialogBinding.ivIcon.setImageResource(R.drawable.ic_outline_info_24)
                    val t1 = "Anda yakin "
                    val t2 = a+b
                    val t3 = "untuk mata kuliah yang diampu?"
                    val text = t1+t2+t3
                    val spannableMessage = SpannableString(text)
                    val start = t1.length
                    val end = start + t2.length
                    spannableMessage.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    dialogBinding.tvText.text = spannableMessage
                    dialogBinding.positiveButton.setOnClickListener {
                        handleSaveSubject(addedElements,deletedElements)
                    }
                    dialogBinding.negativeButton.setOnClickListener {
                        dialogAlert.dismiss()
                    }
                }else{
                    Toast.makeText(context, "Anda tidak melakukan perubahan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleSaveSubject(addedElements:List<Subject>, deletedElements:List<Subject>) {

        var suppSubjectList :MutableList<SuppSubject> = mutableListOf()
        if(deletedElements.isNotEmpty()) {
            deletedElements.forEach {
                suppSubjectList.add(SuppSubject(it.id, false))
            }
        }
        if (addedElements.isNotEmpty()) {
            addedElements.forEach {
                suppSubjectList.add(SuppSubject(it.id, true))
            }
        }
        suppSubjectList.forEach {supp ->
            auth.currentUser?.let { viewModel.handleSaveSubjectLiveData(supp.subjectId, supp.isInsert, it.uid) }
            viewModel.handleSaveSubjectLiveData.observe(viewLifecycleOwner) {
                when (it.status) {
                    StatusResponses.SUCCESS -> {
//                        Toast.makeText(context, it.body.toString(), Toast.LENGTH_SHORT).show()
                    }
                    StatusResponses.EMPTY -> Toast.makeText(
                        context,
                        it.body.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    StatusResponses.ERROR -> Toast.makeText(
                        context,
                        it.msg.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        Intent(context, MainActivity::class.java).also { intent ->
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
                            it.forEachIndexed { _, subject ->
                                oldSubjectList.add(subject)
                            }
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

    override fun checkBoxChecked(subject: Subject, checked: Boolean) {
        when (checked) {
            true -> newSubjectList.add(subject)
            false -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newSubjectList.removeIf {
                    it.id == subject.id
                }
            }
        }
    }
}