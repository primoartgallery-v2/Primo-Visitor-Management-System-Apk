package com.example.primostaffscanner.data.model

import com.google.gson.annotations.SerializedName

data class CheckInRequest(
    @SerializedName("action") val action: String,
    @SerializedName("visitorId") val visitorId: String
)

data class CheckInResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("visitor") val visitor: VisitorInfo? = null
)

data class VisitorInfo(
    @SerializedName("visitorId") val visitorId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("checkInStatus") val checkInStatus: String? = null,
    @SerializedName("checkInTime") val checkInTime: String? = null
)

data class ScanHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val visitorId: String,
    val name: String,
    val status: String,
    val timestamp: String,
    val checkInTime: String? = null
)
