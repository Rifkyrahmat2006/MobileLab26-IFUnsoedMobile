package com.example.prak_pemmob.network

import com.example.prak_pemmob.util.JualanConstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val instance: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(JualanConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
