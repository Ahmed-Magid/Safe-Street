package com.example.safemvvm.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Report(
    val location: String,
    val reportText: String,
    val reportType: String,
    @PrimaryKey val id: Int? = null
)

