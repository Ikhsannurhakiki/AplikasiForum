package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.MaterialAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Material
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityManageMaterialsBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.DialogAlertBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentManageMaterialsBinding
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class ManageMaterialsActivity : AppCompatActivity(), GeneralInterface {

    private lateinit var binding: ActivityManageMaterialsBinding
    private var subjectId: Int = 0
    private var materialId: Int = 0
    private var sks: String? = null
    private var subjectName: String? = null
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMaterialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Kelola Materi"

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        intent.let {
            subjectId = it.getIntExtra("subjectId", 0)
            subjectName = it.getStringExtra("subjectName")
            sks = it.getStringExtra("sks")
            materialId = it.getIntExtra("materialId", 0)
        }

        displayMaterials(subjectId)
        binding.fabAdd.setOnClickListener {
            showMaterialsDialog(0,"")
        }
    }

    private fun showMaterialsDialog(materialId: Int, materialName:String) {
        val dialogBinding = FragmentManageMaterialsBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.etMaterial.setText(materialName)
        if (materialId != 0) dialogBinding.deleteButton.isVisible = true
        dialogBinding.positiveButton.setOnClickListener {
            binding.progressBar.isVisible = true
            var materialName = dialogBinding.etMaterial.text.toString().trim()
            when (materialId) {
                0 -> addMaterial(subjectId, materialName)
                else -> updateMaterial(materialId, materialName)
            }
            dialogAlert.dismiss()
        }

        dialogBinding.negativeButton.setOnClickListener{
            dialogAlert.dismiss()
        }
        dialogBinding.deleteButton.setOnClickListener{
            dialogAlert.dismiss()
            showConfirmationBox(materialId, materialName)
        }
    }

    private fun updateMaterial(materialId: Int, materialName: String) {
        viewModel.editMaterial(materialId, materialName)
        viewModel.editMaterialLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    displayMaterials(subjectId)
                    Toast.makeText(this, "Materi berhasil diperbaharui", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMaterial(subjectId: Int,materialName: String) {
        viewModel.addMaterial(subjectId, materialName)
        viewModel.addMaterialLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    displayMaterials(subjectId)
                    Toast.makeText(this, "Materi perkuliahan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun showEditMaterialsDialog() {
//        val dialogBinding = FragmentManageMaterialsBinding.inflate(layoutInflater)
//        val builder = AlertDialog.Builder(this)
//            .setView(dialogBinding.root)
//        val dialogAlert = builder.show()
//        dialogBinding.etMaterial.setText(subjectName)
//        if (subjectId != 0) dialogBinding.deleteButton.isVisible = true
//        dialogBinding.positiveButton.setOnClickListener {
//            binding.progressBar.isVisible = true
//            var subjectName = dialogBinding.etSubject.text.toString().trim()
//            var sks = dialogBinding.etSks.text.toString().trim()
//            if (sks.toInt() in 1..6) {
//                when (subjectId) {
//                    0 -> {
//                        addSubject(subjectName, sks)
//                    }
//                    else -> updateSubject(subjectId, subjectName, sks)
//                }
//                dialogAlert.dismiss()
//            } else {
//                Toast.makeText(this, "Minimal 1 SKS dan maksimal 6 SKS aaaaaaa", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//        dialogBinding.negativeButton.setOnClickListener {
//            dialogAlert.dismiss()
//        }
//        dialogBinding.deleteButton.setOnClickListener {
//            dialogAlert.dismiss()
//            showConfirmationBox(subjectId, subjectName)
//        }
//    }


    private fun displayMaterials(subjectId: Int) {
        viewModel.getAllMaterialM(subjectId)
        viewModel.getMaterialMListLiveData.observe(this) { lecturer ->
            when (lecturer.status) {
                StatusResponses.SUCCESS -> {
                    binding.rvList.setHasFixedSize(true)
                    binding.rvList.layoutManager = LinearLayoutManager(this)
                    lecturer.body?.let {
                        val adapter = MaterialAdapter(true, this, it)
                        binding.rvList.adapter = adapter
                        adapter.notifyDataSetChanged()
                        binding.progressBar.isVisible = false
                    }
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

    override fun onListClick(material: Material) {

    }

    override fun onListLongClick(material: Material) {
        showMaterialsDialog(material.materialId, material.material)
    }

    private fun showConfirmationBox(materialId: Int, materialName: String) {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.positiveButton.text = "Lanjut"
        dialogBinding.negativeButton.text = "Batal"
        dialogBinding.ivIcon.setImageResource(R.drawable.ic_outline_delete_forever_24)
        dialogBinding.tvText.text =
            "Apakah anda yakin ingin menghapus materi? ( " + materialName + " ), Segala" +
                    " data yang terikat akan dihapus!"
        dialogBinding.positiveButton.setOnClickListener {
            binding.progressBar.isVisible = true
            deleteMaterial(materialId)
            dialogAlert.dismiss()
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
    }

    private fun deleteMaterial(materialId: Int) {
        viewModel.deleteMaterial(materialId)
        viewModel.deleteMaterialLiveData.observe(this){
            when(it.status){
                StatusResponses.SUCCESS ->{
                    displayMaterials(subjectId)
                    Toast.makeText(this, "Materi berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                StatusResponses.EMPTY -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                StatusResponses.ERROR -> Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}