package com.example.primostaffscanner.data.model

import com.google.gson.annotations.SerializedName

data class UpdateInfo(
    @SerializedName("versionCode") val versionCode: Int = 1,
    @SerializedName("versionName") val versionName: String = "1.0",
    @SerializedName("apkUrl") val apkUrl: String = "",
    @SerializedName("releaseNotes") val releaseNotes: String = "",
    @SerializedName("forceUpdate") val forceUpdate: Boolean = false
)
