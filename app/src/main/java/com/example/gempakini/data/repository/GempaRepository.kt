package com.example.gempakini.data.repository

import com.example.gempakini.data.model.Gempa
import com.example.gempakini.data.remote.BmkgApiService
import com.example.gempakini.data.remote.RetrofitInstance

class GempaRepository(
    private val api: BmkgApiService = RetrofitInstance.api
) {
    suspend fun getGempaList(): List<Gempa> = api.getGempaTerkini().Infogempa.gempa
}
