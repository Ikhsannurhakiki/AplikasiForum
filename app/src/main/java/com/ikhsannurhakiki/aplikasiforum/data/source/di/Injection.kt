package com.ikhsannurhakiki.aplikasiforum.data.source.di

import android.content.Context
import com.ikhsannurhakiki.aplikasiforum.data.source.ForumRepository
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.RemoteDataSource
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.service.ApiConfig
import com.ikhsannurhakiki.aplikasiforum.utils.AppExecutors

object Injection {
    fun provideRepository(context: Context): ForumRepository {

        val remoteRepository = RemoteDataSource.getInstance(ApiConfig())
        val appExecutors = AppExecutors()

        return ForumRepository.getInstance(remoteRepository, appExecutors)
    }
}