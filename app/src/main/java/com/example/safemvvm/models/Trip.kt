package com.example.safemvvm.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Trip (
    val customerId: Int,
    val estimatedTime: String,
    val sourceLongitude: Double,
    val sourceLatitude: Double,
    val destinationLongitude: Double,
    val destinationLatitude: Double
)
