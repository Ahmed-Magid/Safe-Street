package com.example.safemvvm.models

enum class EmergenciesEnum {
    KIDNAPPING,
    HARASSMENT,
    FIRE,
    CAR_FAULT,
    UserDidntArrive;

    override fun toString(): String {
        return when (this) {
            KIDNAPPING -> "Kidnapping"
            HARASSMENT -> "Harassment"
            FIRE -> "Fire"
            CAR_FAULT -> "Car Fault"
            UserDidntArrive -> "User Didn't Arrive"
        }
    }
}