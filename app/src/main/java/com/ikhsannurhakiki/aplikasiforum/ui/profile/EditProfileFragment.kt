package com.ikhsannurhakiki.aplikasiforum.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.ApiResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Auth
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.EditUser
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Lecturer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Student
import com.ikhsannurhakiki.aplikasiforum.databinding.*
import com.ikhsannurhakiki.aplikasiforum.interfaces.PermissionInterface
import com.ikhsannurhakiki.aplikasiforum.ui.auth.AuthActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.io.ByteArrayOutputStream
import java.io.File

class EditProfileFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding get() = _binding
    private lateinit var _binding2: BottomSheetDialogProfileBinding
    private val binding2 get() = _binding2
    private lateinit var _binding3: BottomSheetDialogImagepickeroptionsBinding
    private val binding3 get() = _binding3
    private lateinit var _binding4: BottomSheetDialogChangePasswordBinding
    private val binding4 get() = _binding4
    private lateinit var auth: FirebaseAuth
    private lateinit var imageUri: Uri
    private lateinit var viewModel: HomeViewModel
    private lateinit var strImageBitmap: String
    private lateinit var accessRight: String
    private lateinit var authViewModel: AuthViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity as MainActivity
        val pref = UserPreferences.getInstance(activity.dataStore)
        authViewModel =
            ViewModelProvider(this, AuthViewModelFactory(pref))[AuthViewModel::class.java]
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isLoading(true)
        auth = FirebaseAuth.getInstance()

        checkImage()
        viewModel.checkAccessRight(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) {
                when (it.status) {
                    StatusResponses.SUCCESS -> {
                        if (it.body == "Dosen") {
                            accessRight = "Lecturer"
                            getDetailLecturer()
                        } else {
                            accessRight = "Student"
                            getDetailStudent()
                        }
                    }

                    StatusResponses.EMPTY -> {
                        Snackbar.make(requireView(), "EMPTY", Snackbar.LENGTH_SHORT)
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

        binding.picker.setOnClickListener {
            _binding3 = BottomSheetDialogImagepickeroptionsBinding.inflate(
                layoutInflater
            )
            val dialog = BottomSheetDialog(requireContext())
            dialog.setContentView(binding3.root)
            dialog.show()

            binding3.btnCamera.setOnClickListener {
                checkPermissionAndOpenCamera()
                dialog.dismiss()
            }

            binding3.btnGallery.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_GALLERY)
                dialog.hide()
                dialog.dismiss()
            }

            binding3.btnDelete.setOnClickListener {
                dialog.hide()
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.txtDeleteImageConfirmation))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        val ref =
                            FirebaseStorage.getInstance().reference.child("img/${auth.currentUser?.uid}")
                        ref.delete().addOnCompleteListener {
                            if (it.isSuccessful) {
                                dialog.dismiss()
                                binding.circleImageView.setImageResource(R.drawable.img)
                                Toast.makeText(
                                    context,
                                    getString(R.string.profileImageDeleted),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(R.string.profileImageDeleteError),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        binding.btnChangePassword.setOnClickListener {
            changePasswordShowDialog()
        }


        binding.btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(getString(R.string.txtconfirmationlogout))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    updateDeviceToken(auth.currentUser!!.uid)
                    auth.signOut()
                    authViewModel.clearSession()
                    Intent(context, AuthActivity::class.java).also { intent ->
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun getDetailStudent() {
        viewModel.getDetailStudent(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { detailUser ->
                when (detailUser.status) {
                    StatusResponses.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        detailUser.body?.let {
                            binding.apply {

                                //SET VALUE IN PROFILE FRAGMENT
                                tvId.text = it.npm
                                tvEmail.text = it.email
                                tvName.text = it.name
                                tvInfo.text = it.info
                                isLoading(false)
                                btnEditProfile.setOnClickListener {
                                    editProfileStudent(detailUser)
                                }
                            }
                        }
                    }
                    StatusResponses.EMPTY -> binding.progressBar.visibility = View.VISIBLE

                    StatusResponses.ERROR -> Toast.makeText(
                        context,
                        "Koneksi error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getDetailLecturer() {
        viewModel.getDetailLecturer(auth.currentUser?.uid.toString())
            .observe(viewLifecycleOwner) { detailUser ->
                when (detailUser.status) {
                    StatusResponses.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        detailUser.body?.let {
                            binding.apply {

                                //SET VALUE IN PROFILE FRAGMENT
                                textView9.text = "NIDN"
                                tvId.text = it.nip
                                tvEmail.text = it.email
                                tvName.text = it.name
                                tvInfo.text = it.info
                                isLoading(false)
                                btnEditProfile.setOnClickListener {
                                    editProfileLecturer(detailUser)
                                }
                            }
                        }
                    }
                    StatusResponses.EMPTY -> binding.progressBar.visibility = View.VISIBLE

                    StatusResponses.ERROR -> Toast.makeText(
                        context,
                        "Koneksi error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun checkImage() {
        auth.currentUser?.uid.let {
            if (it != null) {
                viewModel.checkProfileImage(it)
                viewModel.imageLiveData.observe(viewLifecycleOwner) { bitmap ->
                    Glide.with(requireContext())
                        .load(bitmap)
                        .placeholder(R.drawable.img)
                        .into(binding.circleImageView)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            uploadImage(bitmap)
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            val uri = data?.data!!
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) {
                    val name = c.getString(nameIndex)
                    inputStream?.let { inputStream ->
                        // create same file with same name
                        val file = File(requireContext().cacheDir, name)
                        val os = file.outputStream()
                        os.use {
                            inputStream.copyTo(it)
                        }
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        uploadImage(bitmap)
                    }
                }
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {

        binding.progressBar.visibility = View.VISIBLE
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img/${auth.currentUser?.uid}")

        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        checkImage()
                        Toast.makeText(
                            context,
                            getString(R.string.profileImageChanged),
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
    }

    private fun changePasswordShowDialog() {
        _binding4 = BottomSheetDialogChangePasswordBinding.inflate(
            layoutInflater
        )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)
        dialog.setContentView(binding4.root)
        dialog.show()

        var oldPass: String? = null
        var newPass: String? = null
        var confNewPass: String? = null

        binding4.apply {
            passwordTextInputLayout3.isEnabled = false
            etOldPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    when {
                        etOldPassword.length() < 6 -> passwordTextInputLayout.error =
                            getString(R.string.passwordTooShort)
                        else -> passwordTextInputLayout.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
            etNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    when {
                        etNewPassword.length() < 6 -> passwordTextInputLayout2.error =
                            getString(R.string.passwordTooShort)
                        else -> passwordTextInputLayout2.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    newPass = etNewPassword.text.toString().trim()
                    confNewPass = etConfirmNewPassword.text.toString().trim()

                    if (newPass != confNewPass) {
                        passwordTextInputLayout3.error = getString(R.string.confpassincorect)
                    } else {
                        passwordTextInputLayout3.error = null
                    }
                    passwordTextInputLayout3.isEnabled = etNewPassword.text.toString().trim() != ""

                }
            })
            etConfirmNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    newPass = etNewPassword.text.toString().trim()
                    confNewPass = etConfirmNewPassword.text.toString().trim()

                    if (newPass != confNewPass) {
                        passwordTextInputLayout3.error = getString(R.string.confpassincorect)
                    } else {
                        passwordTextInputLayout3.error = null
                    }
                }
            })
            btnSaveEdit.setOnClickListener {
                when {
                    passwordTextInputLayout.error != null -> Toast.makeText(
                        context,
                        "Periksa lagi inputtan anda",
                        Toast.LENGTH_SHORT
                    ).show()
                    passwordTextInputLayout2.error != null -> Toast.makeText(
                        context,
                        "Periksa lagi inputtan anda",
                        Toast.LENGTH_SHORT
                    ).show()
                    passwordTextInputLayout3.error != null -> Toast.makeText(
                        context,
                        "Periksa lagi inputtan anda",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        val oldPass = etOldPassword.text.toString().trim()
                        val newPass = etNewPassword.text.toString().trim()
                        val confNewPass = etConfirmNewPassword.text.toString().trim()

                        when ("") {
                            oldPass -> passwordTextInputLayout.error =
                                "${getString(R.string.old_password)} ${getString(R.string.required)}"
                            newPass -> passwordTextInputLayout2.error =
                                "${getString(R.string.new_password)} ${getString(R.string.required)}"
                            confNewPass -> passwordTextInputLayout3.error =
                                "${getString(R.string.confirm_pass)} ${getString(R.string.required)}"
                            else -> {
                                when {
                                    newPass.length < 6 -> passwordTextInputLayout2.error =
                                        getString(R.string.passwordTooShort)
                                    else -> {
                                        val email = auth.currentUser?.email
                                        email?.let { it1 ->
                                            auth.signInWithEmailAndPassword(it1, oldPass)
                                                .addOnCompleteListener(requireActivity()) {
                                                    if (it.isSuccessful) {
                                                        changePassword(newPass)
                                                    } else {
                                                        passwordTextInputLayout.error = "Sandi lama salah"
                                                        Toast.makeText(
                                                            context,
                                                            "Periksa kembali sandi lama anda!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun changePassword(newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, getString(R.string.passhaschanged), Toast.LENGTH_SHORT)
                        .show()
                    updateDeviceToken(auth.currentUser!!.uid)
                    auth.signOut()
                    authViewModel.clearSession()
                    Intent(context, AuthActivity::class.java).also { intent ->
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.passfailedtochange),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }


    }

    private fun editProfileStudent(detailUser: ApiResponses<Student>?) {
        _binding2 = BottomSheetDialogProfileBinding.inflate(
            layoutInflater
        )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)
        dialog.setContentView(binding2.root)
        dialog.show()
        //SET VALUE IN BOTTOM SHEET EDIT PROFILE
        detailUser?.body?.let {
            binding2.apply {
                etId.setText(it.npm)
                etEmail.setText(it.email)
                etName.setText(it.name)
                etInfo.setText(it.info)
                val accessRights = it.accessRights
                val deviceToken = it.deviceToken

                btnSaveEdit.setOnClickListener {
                    if (etId.text.isEmpty()){
                        etId.error = "Field harus diisi"
                    }else if (etName.text.isEmpty()){
                        etName.error = "Field harus diisi"
                    }else if (etInfo.text.isEmpty()){
                        etInfo.error = "Field harus diisi"
                    }else{
                        val user = EditUser(
                            auth.currentUser?.uid ?: String(),
                            etName.text.toString().trim(),
                            etEmail.text.toString().trim(),
                            etInfo.text.toString().trim()
                        )

                        viewModel.updateDetailUser(user)
                        dialog.hide()
                        getDetailStudent()
                    }
                }
            }
        }
    }

    private fun editProfileLecturer(
        detailUser: ApiResponses<Lecturer>
    ) {
        //INITIALIZE BOTTOM SHEET
        _binding2 = BottomSheetDialogProfileBinding.inflate(
            layoutInflater
        )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)
        dialog.setContentView(binding2.root)
        dialog.show()

//        viewModel.subjectLiveData.observe(viewLifecycleOwner) { subject ->
//            when (subject.status) {
//                StatusResponses.SUCCESS -> {
//                    binding2.rvSubject.setHasFixedSize(true)
//                    binding2.rvSubject.layoutManager = LinearLayoutManager(activity)
//                    subject.body?.let {
//                        val adapter = SubjectCheckBoxAdapter()
//                        binding2.rvSubject.adapter = adapter
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//                StatusResponses.ERROR -> {
//                    Toast.makeText(
//                        context,
//                        StatusResponses.ERROR.toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                StatusResponses.EMPTY -> {
//                    Toast.makeText(context, "Data tidak tersedia", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        //SET VALUE IN BOTTOM SHEET EDIT PROFILE
        detailUser.body?.let {
            binding2.apply {
                etId.hint = "NIP"
                etId.isEnabled = false
                etId.setText(it.nip)
                etEmail.setText(it.email)
                etName.setText(it.name)
                etInfo.setText(it.info)
                btnSaveEdit.setOnClickListener {
                    val user = EditUser(
                        auth.currentUser?.uid ?: String(),
                        etName.text.toString().trim(),
                        etEmail.text.toString().trim(),
                        etInfo.text.toString().trim()
                    )
                    viewModel.updateDetailUser(user)
                    var auth2 = Auth(
                        auth.currentUser?.uid ?: String(),
                        etName.text.toString().trim(),
                        "Lecturer",
                        true
                    )
                    authViewModel.saveSession(auth2)
                    Toast.makeText(context, auth2.name, Toast.LENGTH_SHORT).show()
                    dialog.hide()
                    getDetailLecturer()
                }
            }
        }
    }

    private fun updateDeviceToken(uid: String) {
        viewModel.updateUserDeviceToken(uid, "-")
    }

    private fun startCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it).also {
                    startActivityForResult(intent, REQUEST_CAMERA)
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
                startCamera()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
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
            PermissionInterface.checkCameraPermission(requireContext()) -> startCamera()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> showPermissionRationale()
            else -> PermissionInterface.requestCameraPermission(this)
        }
    }

    private fun showPermissionRationale() {
        val dialogBinding = DialogAlertBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.positiveButton.text = "Lanjut"
        dialogBinding.tvText.text =
            "Untuk mengirim gambar melalui kamera, Izinkan aplikasi forum untuk mengakses kamera anda"
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
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
        val dialogAlert = builder.show()
        dialogBinding.tvText.text =
            "Untuk mengirim gambar melalui kamera, Izinkan aplikasi forum untuk mengakses kamera anda." +
                    " Ketuk Pengatuan > Izin, dan nyalakan kamera"
        dialogBinding.positiveButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
            dialogAlert.dismiss()
        }
        dialogBinding.negativeButton.setOnClickListener {
            dialogAlert.dismiss()
        }
    }

    private fun isLoading(b: Boolean) {
        binding.apply {
            if (b) {
                progressBar.visibility = View.VISIBLE
                layout.visibility = View.GONE

            } else {
                progressBar.visibility = View.GONE
                layout.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val REQUEST_CAMERA = 100
        const val REQUEST_GALLERY = 200
    }
}