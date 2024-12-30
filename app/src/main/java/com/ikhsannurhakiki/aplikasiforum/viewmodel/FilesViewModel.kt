package com.ikhsannurhakiki.aplikasiforum.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class FilesViewModel(private val activityResultRegistry: ActivityResultRegistry) : ViewModel() {
    private val PICK_FILES_REQUEST_CODE = 1
    private val TAKE_PICTURE_REQUEST_CODE = 2
    private val selectedFiles = MutableLiveData<List<File>>()
    private lateinit var activity: Activity
    private val _imageUri = MutableLiveData<List<Uri>?>()
    val imageUri: MutableLiveData<List<Uri>?> = _imageUri
    private val _pictureUri = MutableLiveData<Uri?>()
    val pictureUri: LiveData<Uri?> = _pictureUri
    private var pictureLauncher: ActivityResultLauncher<Uri>? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    fun selectImage() {
        selectMultipleImageLauncher.launch("image/*")
    }

    private val selectMultipleImageLauncher = activityResultRegistry.register(
        "selectImages",
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (imageUri.value == null) {
                _imageUri.value = uris
            } else {
                _imageUri.value = _imageUri.value?.plus(uris)
            }
        } else {
            Log.d("444", "bbb")
        }
    }

    fun getSelectedFiles(): MutableLiveData<List<File>> {
        return selectedFiles
    }

    fun onChooseFilesButtonClick(context: Context) {
        activity.let {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            it.startActivityForResult(intent, PICK_FILES_REQUEST_CODE)
        }

    }

    fun takePicture(fragment: Fragment, registry: ActivityResultRegistry): Uri {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val pictureUri = createPictureUri(fragment.requireContext())
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        _pictureUri.value = pictureUri
        if (_imageUri.value != null){
            _imageUri.value = _imageUri.value!! + listOf<Uri>(pictureUri)
        }else{
            _imageUri.value = listOf<Uri>(pictureUri)
        }
      fragment.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        return pictureUri
    }

    private fun createPictureUri(context: Context): Uri {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val pictureFile = File.createTempFile("picture_", ".jpg", picturesDir)
        return FileProvider.getUriForFile(context, "com.ikhsannurhakiki.aplikasiforum.fileprovider", pictureFile)
    }

    private fun processPicture(pictureUri: Uri) {
        // Process the picture, e.g. save it to a database or display it in an ImageView
    }

    fun handleSelectedFiles(context: Context, file: List<File>) {
        // Upload files to server using multipart request
    }

    fun setImageListNull() {
        _imageUri.value = null
    }
}