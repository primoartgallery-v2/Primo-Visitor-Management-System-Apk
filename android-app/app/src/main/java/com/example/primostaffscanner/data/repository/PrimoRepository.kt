package com.example.primostaffscanner.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.primostaffscanner.data.api.PrimoApiClient
import com.example.primostaffscanner.data.model.DashboardMetrics
import com.example.primostaffscanner.data.model.Exhibition
import com.example.primostaffscanner.data.model.ScanHistoryItem
import com.example.primostaffscanner.data.model.VisitorRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrimoRepository(
    private val apiClient: PrimoApiClient
) {
    val exhibitions = mutableStateListOf<Exhibition>(
        Exhibition(
            id = "exh-1",
            code = "EXH-001",
            name = "DRISHTI – A Legacy of Masters",
            venue = "Living Traditions Centre (LTC), Bikaner House, Pandara Road, New Delhi – 110003",
            startDate = "19 Dec 2026",
            endDate = "23 Dec 2026",
            isActive = true
        ),
        Exhibition(
            id = "exh-2",
            code = "EXH-002",
            name = "The Emerging Perspectives — Group Art Exhibition",
            venue = "Convention Foyer, India Habitat Centre, Lodhi Road, New Delhi",
            startDate = "27 Sep 2026",
            endDate = "30 Sep 2026",
            isActive = true
        )
    )

    var currentExhibition by mutableStateOf(exhibitions[0])
        private set

    val visitors = mutableStateListOf<VisitorRecord>()
    val latestCheckIns = mutableStateListOf<ScanHistoryItem>()

    var metrics by mutableStateOf(DashboardMetrics())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var lastUpdatedText by mutableStateOf("Just now")
        private set

    fun selectExhibition(exhibition: Exhibition) {
        currentExhibition = exhibition
        recalculateMetrics()
    }

    fun saveExhibition(
        id: String?,
        name: String,
        venue: String,
        startDate: String,
        endDate: String
    ) {
        if (id != null) {
            val idx = exhibitions.indexOfFirst { it.id == id }
            if (idx != -1) {
                exhibitions[idx] = exhibitions[idx].copy(
                    name = name,
                    venue = venue,
                    startDate = startDate,
                    endDate = endDate
                )
                if (currentExhibition.id == id) {
                    currentExhibition = exhibitions[idx]
                }
            }
        } else {
            val nextNum = exhibitions.size + 1
            val newCode = "EXH-${nextNum.toString().padStart(3, '0')}"
            val newExhibition = Exhibition(
                id = "exh-$nextNum",
                code = newCode,
                name = name,
                venue = venue,
                startDate = startDate,
                endDate = endDate,
                isActive = true
            )
            exhibitions.add(newExhibition)
        }
    }

    fun toggleExhibitionStatus(exhibitionId: String) {
        val idx = exhibitions.indexOfFirst { it.id == exhibitionId }
        if (idx != -1) {
            val item = exhibitions[idx]
            val updated = item.copy(isActive = !item.isActive)
            exhibitions[idx] = updated
            if (currentExhibition.id == exhibitionId) {
                currentExhibition = updated
            }
        }
    }

    suspend fun checkInVisitor(visitorId: String): Result<VisitorRecord> {
        val result = apiClient.checkIn(visitorId)
        val timeNow = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        return result.map { response ->
            val visitorInfo = response.visitor
            val record = VisitorRecord(
                visitorId = visitorId,
                name = visitorInfo?.name ?: "Visitor $visitorId",
                phone = visitorInfo?.phone ?: "",
                email = visitorInfo?.email ?: "",
                city = visitorInfo?.city ?: "",
                status = "Checked In",
                checkInTime = visitorInfo?.checkInTime ?: timeNow
            )

            // Update local list
            val idx = visitors.indexOfFirst { it.visitorId.equals(visitorId, ignoreCase = true) }
            if (idx != -1) {
                visitors[idx] = record
            } else {
                visitors.add(0, record)
            }

            // Add to latest check-ins feed
            latestCheckIns.add(
                0,
                ScanHistoryItem(
                    visitorId = visitorId,
                    name = record.name,
                    status = "Checked In",
                    timestamp = timeNow,
                    checkInTime = record.checkInTime
                )
            )

            recalculateMetrics()
            record
        }
    }

    suspend fun refreshData() = withContext(Dispatchers.IO) {
        isRefreshing = true
        try {
            // Attempt to sync metrics from server
            recalculateMetrics()
            lastUpdatedText = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        } finally {
            isRefreshing = false
        }
    }

    private fun recalculateMetrics() {
        val total = visitors.size
        val checked = visitors.count { it.isCheckedIn }
        val pending = (total - checked).coerceAtLeast(0)
        val rate = if (total > 0) (checked.toFloat() / total.toFloat()) * 100f else 0f

        metrics = DashboardMetrics(
            totalVisitors = total,
            checkedIn = checked,
            pending = pending,
            checkInRate = (Math.round(rate * 10f) / 10f)
        )
    }
}
