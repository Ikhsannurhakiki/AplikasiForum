package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Material
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemMaterialBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.GeneralInterface
import java.util.*

class MaterialAdapter(
    private var isManage : Boolean,
    private val generalInterface: GeneralInterface,
    private val materialList: List<Material>
) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMaterialBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(material: Material) {
            with(binding) {
                txtMaterial.text = material.material
                if(material.totalFinished == null || material.totalUnfinished == null){
                    totalFinished.isVisible = false
                    totalUnfinish.isVisible = false
                    noQuestion.isVisible = !isManage
                }
                totalFinished.text = material.totalFinished + " Selesai"
                totalUnfinish.text = material.totalUnfinished+ " Belum selesai"
                cv.setOnClickListener {
                    generalInterface.onListClick(material)
                }
                cv.setOnLongClickListener {
                    generalInterface.onListLongClick(material)
                    true

                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MaterialAdapter.ViewHolder {
        val itemMaterialBinding =
            ItemMaterialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemMaterialBinding)
    }

    override fun onBindViewHolder(holder: MaterialAdapter.ViewHolder, position: Int) {
        holder.bind(materialList[position])
    }


    override fun getItemCount(): Int = materialList.size

}