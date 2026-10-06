package com.example.gempakini.data.remote

import com.example.gempakini.data.model.GempaResponse
import retrofit2.http.GET

interface BmkgApiService {
    @GET("DataMKG/TEWS/gempaterkini.json")
    suspend fun getGempaTerkini(): GempaResponse
}
