package com.example.safemvvm.models

import android.location.Location

data class EmergencyFired(val location:Location, val time: String, val type: EmergenciesEnum)
