package com.example.safemvvm.models

enum class EmergenciesEnum {
    KIDNAPPING,
    HARASSMENT,
    FIRE,
    CAR_FAULT,
    ROBBERY,
    MURDER,
    UserDidntArrive;

    override fun toString(): String {
        return when (this) {
            KIDNAPPING -> "Kidnapping"
            HARASSMENT -> "Harassment"
            FIRE -> "Fire"
            CAR_FAULT -> "CarFault"
            ROBBERY -> "Robbery"
            MURDER -> "Murder"
            UserDidntArrive -> "UserDidntArrive"
        }
    }
}