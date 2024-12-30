package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.Subject2Adapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityManageSubjectBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.DialogAlertBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentManageSubjectBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory


class ManageSubjectActivity : AppCompatActivity(), SubjectManagementInterface {

    private lateinit var binding: ActivityManageSubjectBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Kelola Mata Kuliah"

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        getSubject()

        binding.fabAdd.setOnClickListener {
            showSubjectManagement(0, "", "")
        }
    }

    private fun getSubject() {
        binding.progressBar.isVisible = true
        viewModel.getSubject()
        viewModel.subjectLiveData.observe(this) { subject ->
            when (subject.status) {
                StatusResponses.SUCCESS -> {
                    binding.rvSubjectList.setHasFixedSize(true)
                    binding.rvSubjectList.layoutManager = LinearLayoutManager(this)
                    subject.body?.let {
                        val adapter = Subject2Adapter(this, it, "admin")
                        binding.rvSubjectList.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                    binding.progressBar.isVisible = false
                }
                StatusResponses.ERROR -> {
                    Toast.makeText(
                        this,
                        StatusResponses.ERROR.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                StatusResponses.EMPTY -> {
                    Toast.makeText(this, "Data tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSubjectManagement(subjectId: Int, subjectName: String, sks: String) {
        val dialogBinding = FragmentManageSubjectBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.etSubject.setText(subjectName)
        dialogBinding.etSks.setText(sks)
        if (subjectId != 0) dialogBinding.deleteButton.isVisible = true
        dialogBinding.positiveButton.setOnClickListener {
            binding.progressBar.isVisible = true
            var subjectName = dialogBinding.etSubject.text.toString().trim()
            var sks = dialogBinding.etSks.text.toString().trim()
            if (sks.toInt() in 1..6) {
                when (subjectId) {
                    0 -> {
                        addSubject(subjectName, sks)
                    }
                    else -> updateSubject(subjectId, subjectName, sks)
                }
                dialogAlert.dismiss()
            }else{
                Toast.makeText(this, "Minimal 1 SKS dan maksimal 6 SKS aaaaaaa", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogBinding.deleteButton.setOnClickListener {
            dialogAlert.dismiss()
            showConfirmationBox(subjectId, subjectName)
        }
    }

    private fun showConfirmationBox(subjectId: Int, subjectName: String) {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.positiveButton.text = "Lanjut"
        dialogBinding.negativeButton.text = "Batal"
        dialogBinding.ivIcon.setImageResource(R.drawable.ic_outline_delete_forever_24)
        dialogBinding.tvText.text = "Apakah anda yakin ingin menghapus mata kuliah? ( "+subjectName+" ), Segala" +
                " data yang terikat akan dihapus!"
        dialogBinding.positiveButton.setOnClickListener {
            binding.progressBar.isVisible = true
            deleteSubject(subjectId)
            dialogAlert.dismiss()
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
    }

    private fun deleteSubject(subjectId: Int) {
        viewModel.deleteSubject(subjectId)
        viewModel.deleteSubjectLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    getSubject()
                    Toast.makeText(this, "Mata kuliah berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSubject(subjectId: Int, subjectName: String, sks: String) {
        viewModel.editSubject(subjectId, subjectName, sks)
        viewModel.editSubjectLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    getSubject()
                    Toast.makeText(this, "Mata kuliah berhasil diperbaharui", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSubject(subjectName: String, sks: String) {
        viewModel.addSubject(subjectName, sks)
        viewModel.addSubjectLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    getSubject()
                    Toast.makeText(this, "Mata kuliah berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(subjectId: Int, subjectName: String, sks: String) {
        val bundle = Bundle()
        bundle.putInt("subjectId", subjectId)
        bundle.putString("subjectName", subjectName)
        bundle.putString("sks", sks)
        startActivity(Intent(this, ManageMaterialsActivity::class.java).apply {
         putExtras(bundle)
        })
    }


    override fun onLongClick(subjectId: Int, subjectName: String, sks: String) {
        showSubjectManagement(subjectId, subjectName, sks)
    }
}