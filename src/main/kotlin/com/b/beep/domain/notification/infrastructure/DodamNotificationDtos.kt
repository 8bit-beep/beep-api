package com.b.beep.domain.notification.infrastructure

data class DodamNotificationRequest(
    val appPublicId: String,
    val title: String,
    val body: String,
    val targetUserPublicIds: List<String>,
    val data: Map<String, String> = emptyMap(),
)

data class DodamNotificationResponse(
    val status: Int,
    val message: String,
    val data: DodamNotificationResult? = null,
)

data class DodamNotificationResult(
    val successCount: Int,
    val failureCount: Int,
)
