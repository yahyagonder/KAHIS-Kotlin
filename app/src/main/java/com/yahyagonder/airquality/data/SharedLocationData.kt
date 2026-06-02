package com.yahyagonder.airquality.data

data class SharedLocationData(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pm25: Int = 0,
    val voc: Int = 0,
    val uv: Int = 0,
    val sharedBy: String = "",
    val timestamp: Long = 0L
)
