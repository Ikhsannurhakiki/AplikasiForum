package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentImagePopUpDialogBinding
import java.io.File
import java.io.IOException

class ImagePopUpDialogFragment : DialogFragment() {

    private lateinit var _binding: FragmentImagePopUpDialogBinding
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePopUpDialogBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val userId = it.getString(USERID)

            val ref = FirebaseStorage.getInstance().reference.child("img/${userId}")
            try {
                val localFile = File.createTempFile("tempImage", ".jpeg")
                ref.getFile(localFile)
                    .addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        binding.imagePopUp.setImageBitmap(bitmap)
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val USERID = "USER_ID"
    }
}
