package com.ikhsannurhakiki.aplikasiforum.ui.home

interface LecturerPopUpInterface {

    fun onLecturerClick(lecturerId: String, lecturerName:String, subjectId: Int, suppLecturer: Int)

    fun onDetailsButtonClick(lecturerId: String)
}