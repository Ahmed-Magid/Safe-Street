package com.example.safemvvm.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrustedContact(
    val username: String,
    val email: String,
    @PrimaryKey val id: Int? = null
)

