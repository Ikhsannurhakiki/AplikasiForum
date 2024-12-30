package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.ImageSliderAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.ImageFileList
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityAddQuestionBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.BottomSheetDialogImagepickeroptionsBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.DialogAlertBinding
import com.ikhsannurhakiki.aplikasiforum.interfaces.PermissionInterface
import com.ikhsannurhakiki.aplikasiforum.ui.main.FcmNotificationSender
import com.ikhsannurhakiki.aplikasiforum.utils.reduceFileImage
import com.ikhsannurhakiki.aplikasiforum.utils.rotateFile
import com.ikhsannurhakiki.aplikasiforum.utils.timeStamp
import com.ikhsannurhakiki.aplikasiforum.utils.uriToFile
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AddQuestionActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityAddQuestionBinding
    private val getFile = ArrayList<ImageFileList>()
    private var state: Boolean = true
    private lateinit var title: String
    private lateinit var question: String
    private var subjectId: Int = 0
    private var materialId: Int = 0
    private var lecturerId: String? = null
    private var subjectName: String? = null
    private var suppLecturer: Int = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var imageAdapter: ImageSliderAdapter
    private lateinit var dots: ArrayList<TextView>
    private var imageNumbering = 0
    private lateinit var tagsList: List<String>
    private lateinit var _binding3: BottomSheetDialogImagepickeroptionsBinding
    private val binding3 get() = _binding3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (getFile.isEmpty()) {
            binding.viewPager.isVisible = false
            binding.dotsIndicator.isVisible = false
        }
        getFile.clear()
        auth = FirebaseAuth.getInstance()
        intent.let {
            subjectId = it.getIntExtra(SUBJECTID, 0)
            lecturerId = it.getStringExtra(LECTURERID)
            subjectName = it.getStringExtra(SUBJECTNAME)
            suppLecturer = it.getIntExtra(SUPPLECTURER, 0)
            materialId = it.getIntExtra(MATERIALID, 0)
        }

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        binding.btnImagePicker.setOnClickListener {
            _binding3 = BottomSheetDialogImagepickeroptionsBinding.inflate(
                layoutInflater
            )
            binding3.textView.text = resources.getText(R.string.chooseImage)
            binding3.btnDelete.isVisible = false
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(binding3.root)
            dialog.show()

            binding3.btnCamera.setOnClickListener {
                checkPermissionAndOpenCamera()
                dialog.dismiss()

            }

            binding3.btnGallery.setOnClickListener {
                startGallery()
                dialog.dismiss()
            }
        }
        binding.btnAdd.setOnClickListener {
            binding.apply {
                loadingState(true)
                binding.progressBar.visibility = View.VISIBLE
                title = etTitle.text.toString().trim()
                question = etQuestion.text.toString().trim()
                val tags1 = etTags.text.toString().trim().split("#").toTypedArray()
                tagsList = tags1.toMutableList().apply {
                    removeAt(0)
                }

                if (title.isNotEmpty() && question.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    insertQuestion()
                    val resultIntent = Intent()
                    resultIntent.putExtra("isInsertQuestionSuccess", "insertSuccess")
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(
                        this@AddQuestionActivity,
                        "Semua field harus diisi",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.progressBar.visibility = View.INVISIBLE

                }
            }
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        val chooser = Intent.createChooser(intent, getString(R.string.choosePict))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra(getString(R.string.pict), File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra(getString(R.string.pict))
            } as? File

            val isBackCamera =
                it.data?.getBooleanExtra(getString(R.string.isBackCamera), true) as Boolean

            myFile?.let { file ->
                imageNumbering += 1
                rotateFile(file, isBackCamera)
                val newFileName = "${timeStamp}_${imageNumbering}.jpg"
                val newFile = File(file.parent, newFileName)
                if (file.renameTo(newFile)) {
                    getFile.add(
                        ImageFileList(newFile)
                    )
                    showImage()
                } else {
                    Toast.makeText(this, "Renaming image failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    val count = clipData.itemCount
                    for (i in 0 until count) {
                        val uri = clipData.getItemAt(i).uri
                        val file = uriToFile(uri, this)
                        getFile.add(
                            ImageFileList(file)
                        )
                    }
                    showImage()
                } else {
                    data.data?.let { uri ->
                        val file = uriToFile(uri, this)
                        getFile.add(
                            ImageFileList(file)
                        )
                        showImage()
                    }
                }
            }
        }
    }

    private fun showImage() {
        imageAdapter = ImageSliderAdapter(getFile)
        binding.viewPager.adapter = imageAdapter
        dots = ArrayList()
        binding.dotsIndicator.removeAllViews()
        setIndicator()

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (getFile.isNotEmpty()) {
                    for (i in 0 until getFile.size) {
                        if (i == position)
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    this@AddQuestionActivity,
                                    R.color.green_200
                                )
                            )
                        else
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    this@AddQuestionActivity,
                                    R.color.lightGrey
                                )
                            )
                    }
                }
                super.onPageSelected(position)
            }
        })
    }

    private fun setIndicator() {
        if (getFile.isNotEmpty()) {
            binding.viewPager.isVisible = true
            binding.dotsIndicator.isVisible = true
            for (i in 0 until getFile.size) {
                dots.add(TextView(this))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dots[i].text = Html.fromHtml("&#9679", Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    dots[i].text = Html.fromHtml("&#9679")
                }
                dots[i].textSize = 18f
                binding.dotsIndicator.addView(dots[i])
            }
        } else {
            Log.d("getFile", "Empty")
        }
    }

    private fun uploadImages(lastQuestionId: String) {
        var totalImage = getFile.size
        if (getFile.isNotEmpty()) {
            for (i in 0 until getFile.size) {
                val file = reduceFileImage(getFile[i].file)
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                viewModel.uploadImages(imageMultipart, lastQuestionId, "true").observe(this) {
                    loadingState(false)
                    when (it.status) {
                        StatusResponses.SUCCESS -> {
                            if (i == getFile.size - 1) {
                                totalImage -= 1
                            }
                        }
                        StatusResponses.ERROR -> Toast.makeText(
                            this,
                            "Failed upload image",
                            Toast.LENGTH_SHORT
                        ).show()
                        StatusResponses.EMPTY -> {}
                    }
                }
            }


        } else {
//            Toast.makeText(this, getString(R.string.setImageFirst), Toast.LENGTH_SHORT).show()
            binding.progressBar.isVisible = false
        }
    }

    private fun loadingState(isLoading: Boolean) {
        if (isLoading) binding.progressBar.visibility =
            View.VISIBLE else binding.progressBar.visibility = View.GONE
    }

    private fun getToken(userId: String?, questionId: Int) {
        viewModel.getToken(userId).observe(this) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    val token = it.body
                    sendNotification(token, questionId)
                }
                StatusResponses.EMPTY -> Toast.makeText(
                    this,
                    getString(R.string.noToken),
                    Toast.LENGTH_SHORT
                ).show()

                StatusResponses.ERROR -> Toast.makeText(
                    this,
                    getString(R.string.errorCon),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendNotification(token: String?, questionId: Int) {
        val fcmNotification = FcmNotificationSender()
        val uid = auth.currentUser?.uid
        val sbjectName = subjectName
        if (token != null && uid != null && sbjectName != null) {
            fcmNotification.fcmNotificationSender(
                token,
                subjectId,
                lecturerId!!,
                uid,
                questionId,
                sbjectName,
                "$subjectName ${auth.currentUser?.displayName!!} Menambahkan pertanyaan",
                title,
                applicationContext,
                this
            )
            fcmNotification.sendNotification()
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
                tagsList,
                suppLecturer,
                materialId
            ).observe(this) {
                when (it.status) {
                    StatusResponses.SUCCESS -> {
                        if (getFile.isNotEmpty()) {
                            uploadImages(it.body.toString())
                        }
                        getToken(lecturerId, it.body!!.toInt())
                    }
                    StatusResponses.ERROR -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT)
                        .show()
                    StatusResponses.EMPTY -> Toast.makeText(
                        this,
                        "Terjadi kesalahan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionInterface.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraX()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    showGoToAppSettingsDialog()
                }
            }
        }
    }

    private fun checkPermissionAndOpenCamera() {
        when {
            PermissionInterface.checkCameraPermission(this) -> startCameraX()
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> showPermissionRationale()
            else -> PermissionInterface.requestCameraPermission(this)
        }
    }

    private fun showPermissionRationale() {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.positiveButton.text = "Lanjut"
        dialogBinding.tvText.text = "Untuk mengirim gambar melalui kamera, Izinkan aplikasi forum untuk mengakses kamera anda"
        dialogBinding.positiveButton.setOnClickListener {
            PermissionInterface.requestCameraPermission(this)
            dialogAlert.dismiss()
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
    }

    private fun showGoToAppSettingsDialog() {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.tvText.text =
        "Untuk mengirim gambar melalui kamera, Izinkan aplikasi forum untuk mengakses kamera anda." +
                " Ketuk Pengatuan > Izin, dan nyalakan kamera"
        dialogBinding.positiveButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            dialogAlert.dismiss()
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
    }

    companion object {
        const val SUPPLECTURER = "1"
        const val SUBJECTNAME = "subjectname"
        const val LECTURERID = "LECTURERID"
        const val SUBJECTID = "1"
        const val CAMERA_X_RESULT = 200
        const val MATERIALID = "2"
    }
}