package com.ikhsannurhakiki.aplikasiforum.ui.home

interface SubjectManagementInterface {
    fun onClick(subjectId: Int,subjectName:String, sks: String)
    fun onLongClick(subjectId: Int,subjectName:String, sks: String)
}