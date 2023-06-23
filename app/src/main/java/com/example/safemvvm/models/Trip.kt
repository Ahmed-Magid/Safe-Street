package com.example.safemvvm.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Trip(
    val source: String,
    val destination: String,
    @PrimaryKey val id: Int? = null
)
