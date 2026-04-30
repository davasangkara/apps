package com.example.apps.network

import com.example.apps.model.Lapangan
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("user")
    suspend fun getLapangan(): Response<List<Lapangan>>
}