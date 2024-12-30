package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Lecturer
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsLecturerBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.LecturerPopUpInterface
import java.io.File
import java.io.IOException

class LecturerAdapter(
    private val popUpInterface: LecturerPopUpInterface,
    private val lecturerList: List<Lecturer>,
    private val subjectId: Int
) : RecyclerView.Adapter<LecturerAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemsLecturerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(lecturer: Lecturer) {
            with(binding) {
                tvName.text = lecturer.name
                val ref = FirebaseStorage.getInstance().reference.child("img/${lecturer.id}")
                try
                {
                    val localFile = File.createTempFile("tempImage", ".jpeg")
                    ref.getFile(localFile)
                        .addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.ciAvatar.setImageBitmap(bitmap)
                        }
                } catch (e: IOException)
                {
                    e.printStackTrace()
                }


                root.setOnClickListener {
                    popUpInterface.onLecturerClick(lecturer.id, lecturer.name, subjectId, lecturer.suppLecturer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsLecturerBinding =
            ItemsLecturerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsLecturerBinding)
    }

    override fun onBindViewHolder(holder: LecturerAdapter.ViewHolder, position: Int) {
        holder.bind(lecturerList[position])
    }

    override fun getItemCount(): Int = lecturerList.size
}