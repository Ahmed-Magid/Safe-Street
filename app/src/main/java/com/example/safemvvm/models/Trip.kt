package com.example.safemvvm.models

data class Trip (
    val customerId: Int,
    val estimatedTime: Int,
    val sourceLongitude: Double,
    val sourceLatitude: Double,
    val destinationLongitude: Double,
    val destinationLatitude: Double
)
