package com.ikhsannurhakiki.aplikasiforum.data.source.remote.response

import android.net.Uri
import java.io.File

data class ImageDataResponse(
    val uri: Uri,
    val name: String,
    val description: String
)

data class ImageFileList(
    val file: File
)

data class ImageFileListUri(
    val uri: Uri
)