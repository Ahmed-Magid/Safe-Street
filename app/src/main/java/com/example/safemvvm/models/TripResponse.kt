package com.example.safemvvm.models

data class TripResponse(
    val id: Int,
    val startedAtDate: String,
    val startedAtTime: String,
    val estimatedEndDate : String,
    val estimatedEndTime : String,
    val estimatedTime: Int,
    val remainingTime: Long,
    val ended : Boolean,
    val sourceLongitude : Double,
    val sourceLatitude : Double,
    val destinationLongitude : Double,
    val destinationLatitude : Double,
    val customerId : Int)
