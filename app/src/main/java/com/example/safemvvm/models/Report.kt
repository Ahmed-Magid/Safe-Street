package com.example.safemvvm.models

import java.io.Serializable


data class Report(
    val customerId: Int,
    var reportText: String,
    val category: String,
    val longitude: String,
    val latitude: String,
    val firstName: String = "",
    val lastName: String = "",
)

