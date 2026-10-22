package com.example.primostaffscanner.data.api

import com.example.primostaffscanner.data.model.CheckInRequest
import com.example.primostaffscanner.data.model.CheckInResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class PrimoApiClient(
    private var webAppUrl: String = DEFAULT_WEB_APP_URL
) {
    companion object {
        const val DEFAULT_WEB_APP_URL =
            "https://script.google.com/macros/s/AKfycbxqdfRHhyFAm61gRTCoB83CQibYcgN_3N51ShvSwJaavBluXh6qZoVePLZoYPfm-ErX/exec"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    fun updateUrl(url: String) {
        if (url.isNotBlank()) {
            webAppUrl = url.trim()
        }
    }

    fun getUrl(): String = webAppUrl

    suspend fun checkIn(visitorId: String): Result<CheckInResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = CheckInRequest(action = "checkIn", visitorId = visitorId)
            val jsonBody = gson.toJson(payload)
            val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(webAppUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Server returned HTTP error ${response.code}: ${response.message}")
                )
            }

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) {
                return@withContext Result.failure(Exception("Empty response from server"))
            }

            val result = gson.fromJson(bodyString, CheckInResponse::class.java)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun lookup(visitorId: String): Result<CheckInResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = CheckInRequest(action = "lookup", visitorId = visitorId)
            val jsonBody = gson.toJson(payload)
            val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(webAppUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Server returned HTTP error ${response.code}")
                )
            }

            val bodyString = response.body?.string().orEmpty()
            val result = gson.fromJson(bodyString, CheckInResponse::class.java)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
