package com.example.primostaffscanner.data.model

data class Exhibition(
    val id: String,
    val code: String,
    val name: String,
    val venue: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true
) {
    val dateRangeText: String
        get() = "$startDate – $endDate"

    val fullSubtitle: String
        get() = "$code · $venue · $dateRangeText · ${if (isActive) "Active" else "Inactive"}"
}

data class DashboardMetrics(
    val totalVisitors: Int = 0,
    val checkedIn: Int = 0,
    val pending: Int = 0,
    val checkInRate: Float = 0f
)

data class VisitorRecord(
    val visitorId: String,
    val name: String,
    val phone: String,
    val email: String,
    val city: String,
    val status: String = "Pending",
    val checkInTime: String? = null
) {
    val isCheckedIn: Boolean
        get() = status.trim().lowercase().let {
            it == "checked in" || it == "checked-in" || it == "checkedin" || it.contains("checked")
        }
}
