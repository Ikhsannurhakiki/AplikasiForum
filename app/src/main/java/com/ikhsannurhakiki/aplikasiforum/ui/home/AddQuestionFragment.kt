package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.adapter.ImagePagerAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentAddQuestionBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.SheetShowImagesBinding
import com.ikhsannurhakiki.aplikasiforum.utils.uriToFile
import com.ikhsannurhakiki.aplikasiforum.viewmodel.FilesViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.FilesViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.io.File


class AddQuestionFragment : Fragment() {

    private lateinit var _binding: FragmentAddQuestionBinding
    private val binding get() = _binding
    private lateinit var _binding2: SheetShowImagesBinding
    private val binding2 get() = _binding2
    private lateinit var auth: FirebaseAuth
    private lateinit var currentAutoIncrement: String
    private lateinit var title: String
    private lateinit var question: String
    private var subjectId: Int = 0
    private var lecturerId: String? = null
    private var subjectName: String? = null
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: HomeViewModel
    lateinit var viewModel2: FilesViewModel
    private val PICK_FILES_REQUEST_CODE = 1
    private val TAKE_PICTURE_REQUEST_CODE = 2
    private val options = arrayOf<CharSequence>("Camera", "Gallery", "Cancel")
    private var getFile = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val activityResultRegistry = requireActivity().activityResultRegistry
        viewModel2 = ViewModelProvider(
            this,
            FilesViewModelFactory(activityResultRegistry)
        )[FilesViewModel::class.java]
        viewModel2.setImageListNull()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddQuestionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        arguments?.let {
            subjectId = it.getInt(SUBJECTID)
            lecturerId = it.getString(LECTURERID)
            subjectName = it.getString(SUBJECTNAME)
        }

        binding.apply {
            btnAdd.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                title = etTitle.text.toString().trim()
                question = etQuestion.text.toString().trim()
                val tags1 = etTags.text.toString().trim().split("#").toTypedArray()
                val tags2 = tags1.toMutableList().apply {
                    removeAt(0)
                }

                if (title.isNotEmpty() && question.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    if (lecturerId != null) {
                        getToken(lecturerId)
                    }

                    //CurrentAutoIncrement
                    viewModel.checkCurrentAutoIncrement()
                        .observe(viewLifecycleOwner) { tags ->
                            when (tags.status) {
                                StatusResponses.SUCCESS -> {
                                    val current = tags.body?.minus(1)
                                    Log.d("current", current.toString())
                                    currentAutoIncrement = tags.body.toString()

                                    insertQuestion()
                                    Log.d("tag2", tags2.count().toString())
                                    var test = 0
                                    for (a in tags2) {
                                        test++
                                        Log.d("tag2", test.toString())
                                        Log.d("tag2", a)
                                        val tagTrim = a.trim()
                                        tagsHandler(tagTrim)
                                    }
                                }
                                StatusResponses.EMPTY -> Log.d("CheckTag", tags.body.toString())
                                StatusResponses.ERROR -> Toast.makeText(
                                    context,
                                    "Koneksi error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            Handler().postDelayed({
                                findNavController().popBackStack()
                            }, 3000)

                        }
                } else {
                    Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT)
                        .show()
                    binding.progressBar.visibility = View.INVISIBLE

                }
            }
            btnImagePicker.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Select Image")
                viewModel2.setActivity(requireActivity())
                builder.setItems(options) { dialog, which ->
                    if (options[which] == "Camera") {
                        viewModel2.takePicture(
                            this@AddQuestionFragment,
                            requireActivity().activityResultRegistry
                        )
                    } else if (options[which] == "Gallery") {
                        viewModel2.selectImage()
                    } else {
                        dialog.dismiss()
                    }
                    viewModel2.getSelectedFiles().observe(viewLifecycleOwner) { uris ->
                        viewModel2.handleSelectedFiles(requireContext(), getFile)
                    }
                }
                builder.show()
                viewModel2.imageUri.observe(viewLifecycleOwner) { uri ->
                    if (uri != null) {
                        showImages(uri)
                        binding.viewPager.adapter = ImagePagerAdapter(uri)
                        TabLayoutMediator(binding.indicator, binding.viewPager) { _, _ -> }
                            .attach()
                    }
                }
            }
        }
    }

    private fun showImages(
        uri: List<Uri>
    ) {
        _binding2 = SheetShowImagesBinding.inflate(
            layoutInflater
        )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)
        dialog.setContentView(binding2.root)

        binding2.viewPager.adapter = ImagePagerAdapter(uri)
        TabLayoutMediator(binding2.indicator, binding2.viewPager) { _, _ -> }
            .attach()

        dialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val file = uriToFile(uri, requireContext())
                    getFile.add(file)
                }
            } else {
                data?.data?.let { uri ->
                    val file = uriToFile(uri, requireContext())
                    getFile.add(file)
                }
            }
            viewModel2.getSelectedFiles().value = getFile
//            if (uris.size != 0) {
//                binding.viewPager.adapter = ImagePagerAdapter(uris)
//                TabLayoutMediator(binding.indicator, binding.viewPager) { _, _ -> }
//                    .attach()
//            }
        } else if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel2.getSelectedFiles().value = getFile
//            if (uris.size != 0) {
//                binding.viewPager.adapter = ImagePagerAdapter(uris)
//                TabLayoutMediator(binding.indicator, binding.viewPager) { _, _ -> }
//                    .attach()
//            }
        }
    }

    private fun getToken(userId: String?) {
        viewModel.getToken(userId).observe(viewLifecycleOwner) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    val token = it.body
                    sendNotification(token)
                }
                StatusResponses.EMPTY -> Toast.makeText(
                    context,
                    "Token tidak ada",
                    Toast.LENGTH_SHORT
                ).show()

                StatusResponses.ERROR -> Toast.makeText(
                    context,
                    "Koneksi error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendNotification(token: String?) {
//        val fcmNotification = FcmNotificationSender()
//        Toast.makeText(context, token, Toast.LENGTH_SHORT).show()
//        if (token != null) {
//            fcmNotification.fcmNotificationSender(
//                token,
//                "${subjectName}${auth.currentUser?.displayName!!} Menambahkan pertanyaan baru",
//                title,
//                requireContext(),
//                requireActivity()
//            )
//            fcmNotification.sendNotification()
//        }
    }

    private fun tagsHandler(tagTrim: String) {

        Log.d("CheckTag", tagTrim)

        viewModel.checkTags(tagTrim)
            .observe(viewLifecycleOwner) { tags ->
                when (tags.status) {
                    StatusResponses.SUCCESS -> {
                        Snackbar.make(requireView(), "SUCCESS", Snackbar.LENGTH_SHORT)
                        viewModel.insertQuestionTag(currentAutoIncrement, tags.body.toString())
                    }
                    StatusResponses.EMPTY -> {
                        Snackbar.make(requireView(), "EMPTY", Snackbar.LENGTH_SHORT)
                        binding.progressBar.visibility = View.VISIBLE
                        viewModel.insertTag(tagTrim)
                        Handler().postDelayed({
                            tagsHandler(tagTrim)
                        }, 1500)
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

    private fun insertQuestion() {
        auth.currentUser?.let { auth ->
            viewModel.insertQuestion(
                auth.uid,
                title,
                question,
                subjectId,
                lecturerId,
                listOf("a","a"),
                0,0            )

        }
    }

    companion object {
        const val SUBJECTNAME = "subjectname"
        const val LECTURERID = "LECTURERID"
        const val SUBJECTID = "1"
        private const val REQUEST_PERMISSIONS = 1
    }

}