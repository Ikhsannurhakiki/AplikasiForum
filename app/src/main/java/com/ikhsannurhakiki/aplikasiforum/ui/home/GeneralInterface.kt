package com.ikhsannurhakiki.aplikasiforum.ui.home

import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Material

interface GeneralInterface {
    fun onListClick(material: Material)

    fun onListLongClick(material: Material)
}