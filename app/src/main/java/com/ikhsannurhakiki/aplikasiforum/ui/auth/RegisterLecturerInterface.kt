package com.ikhsannurhakiki.aplikasiforum.ui.auth

import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject

interface RegisterLecturerInterface {
    fun checkBoxChecked(subject: Subject, checked: Boolean)
}