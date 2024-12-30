package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
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
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityAddAnswerBinding
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

class AddAnswerActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityAddAnswerBinding
    private val getFile = ArrayList<ImageFileList>()
    private var imageNumbering = 0
    private lateinit var imageAdapter: ImageSliderAdapter
    private lateinit var dots: ArrayList<TextView>
    private lateinit var auth: FirebaseAuth
    private var questionId: Int = 0
    private var point = 0
    private var askedById: String? = null
    private var lecturerId: String? = null
    private var subjectName: String? = null
    private var notifTitle: String = ""
    private var subjectId = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var _binding3: BottomSheetDialogImagepickeroptionsBinding
    private val binding3 get() = _binding3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnswerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.isVisible = false

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        getFile.clear()
        intent.let {
            questionId = it.getIntExtra("QUESTIONID", 0)
            askedById = it.getStringExtra("ASKEDBYID")
            lecturerId = it.getStringExtra("LECTURERID")
            subjectName = it.getStringExtra("SUBJECTNAME")
            subjectId = it.getIntExtra("SUBJECTID", 0)
            point = it.getIntExtra("POINT", 0)
        }
        binding.btnImagePicker.setOnClickListener {
            saveText(binding.etAnswer.text.toString().trim())
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
            val etAnswer = binding.etAnswer.text.toString().trim()
            notifTitle = etAnswer
            if (etAnswer.isNotEmpty()) {
                viewModel.insertAnswer(etAnswer, auth.currentUser?.uid, questionId)
                    .observe(this) {
                        when (it.status) {
                            StatusResponses.SUCCESS -> {
                                if (point == 5) {    //DOSEN
                                    getToken(askedById)  //GET TOKEN MHS
                                } else {
                                    getToken(lecturerId)  //GET TOKEN DOSEN
                                }
                                if (getFile.isNotEmpty()) {
                                    uploadImages(it.body.toString())
                                }else{
                                    val resultIntent = Intent()
                                    resultIntent.putExtra("isQuestion", false);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }

                            }
                            StatusResponses.EMPTY -> {
                                showToast("Terjadi kesalahan")
                            }
                            StatusResponses.ERROR -> {
                                showToast("Terjadi kesalahan")
                            }
                        }
                    }
            } else {
                showToast("Halaman jawaban wajib diisi")
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

        binding.viewPager.isVisible = true
        imageAdapter = ImageSliderAdapter(getFile)
        binding.viewPager.adapter = imageAdapter
        dots = ArrayList()
        binding.dotsIndicator.removeAllViews()
        setIndicator()

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (getFile.isNotEmpty()) {
                    Log.d("sssssssssss", getFile.size.toString())
                    for (i in 0 until getFile.size) {
                        if (i == position)
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    this@AddAnswerActivity,
                                    R.color.green_200
                                )
                            )
                        else
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    this@AddAnswerActivity,
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
        Log.d("aaaaaaaaaaaaaaa", getFile.size.toString())
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
                viewModel.uploadImages(imageMultipart, lastQuestionId, "false").observe(this) {
                    loadingState(false)
                    when (it.status) {
                        StatusResponses.SUCCESS -> {
                            if (i == getFile.size - 1) {
                                totalImage -= 1
                                val resultIntent = Intent()
                                resultIntent.putExtra("isQuestion", false);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        }
                        StatusResponses.ERROR -> showToast("Failed upload image")
                        StatusResponses.EMPTY -> showToast("Failed upload image")
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

    override fun onBackPressed() {
        super.onBackPressed()
        saveText("")
    }

    private fun saveText(txt:String) {
        if (binding.etAnswer.text != null) {
            sharedPref = getSharedPreferences("answerPref", Context.MODE_PRIVATE)
            editor = sharedPref.edit()
            editor.putString("answerKey",txt)
            editor.apply()
        }
//        if (getFile.size != 0) {
//            for (i in 0 until getFile.size) {
//                viewModel.getFile.add(
//                    ImageFileList(getFile[i].file)
//                )
//            }
//        }
    }

    private fun sendNotification(token: String?) {
        val fcmNotification = FcmNotificationSender()
        val askedBy = askedById
        val sbjectName = subjectName
        if (token != null && askedBy != null && sbjectName != null) {
            fcmNotification.fcmNotificationSender(
                token,
                subjectId,
                lecturerId!!,
                askedBy,
                questionId,
                sbjectName,
                "$subjectName ${auth.currentUser?.displayName!!} Menambahkan pertanyaan",
                notifTitle,
                applicationContext,
                this
            )
            fcmNotification.sendNotification()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("answerPref", Context.MODE_PRIVATE)
        val yourString = sharedPref.getString("answerKey", "")
        binding.etAnswer.text = SpannableStringBuilder.valueOf(yourString)

        if (viewModel.getFile.isNotEmpty()) {
            for (i in 0 until viewModel.getFile.size) {
                getFile.add(
                    ImageFileList(viewModel.getFile[i].file)
                )
            }
        }
    }

    private fun getToken(userId: String?) {
        viewModel.getToken(userId).observe(this) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    val token = it.body
                    sendNotification(token)
                }
                StatusResponses.EMPTY -> showToast("Token tidak ada")
                StatusResponses.ERROR -> showToast("Koneksi error")
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val POINT = "POINT"
        const val ASKEDBYID = "ASKEDBYID"
        const val QUESTIONID = "QUESTIONID"
        const val SUBJECTNAME = "SUBJECTNAME"
        const val LECTURERID = "LECTURERID"
        const val SUBJECTID = "1"
        private const val REQUEST_PERMISSIONS = 1
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}