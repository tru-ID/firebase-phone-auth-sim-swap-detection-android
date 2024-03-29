package com.example.firebaseandroid.API.retrofit

import com.example.firebaseandroid.API.data.SIMCheckPost
import com.example.firebaseandroid.API.data.SIMCheckResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("/sim-check")
    suspend fun createSIMCheck(@Body user: SIMCheckPost): Response<SIMCheckResult>

    companion object {
        const val base_url = "https://{subdomain}.{region}.ngrok.io"
    }
}
