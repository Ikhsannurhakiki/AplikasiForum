package com.ikhsannurhakiki.aplikasiforum.data.source.remote.service

import com.ikhsannurhakiki.aplikasiforum.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {

    private var retrofit: Retrofit? = null

    private fun getApiService(): Retrofit {
        if (retrofit == null) {
            val loggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit as Retrofit
        }
        return retrofit as Retrofit
    }

    fun client() = getApiService().create(ApiService::class.java)
}