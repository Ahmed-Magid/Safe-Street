package com.example.safemvvm.utils

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class LocalDatabaseManager(val context: AppCompatActivity) {
    private val localDB: SharedPreferences = context.getSharedPreferences("localDB", 0)

    fun id(id: Int): LocalDatabaseManager {
        localDB.edit().apply {
            putInt("userId", id)
            apply()
        }
        return this
    }

    fun token(token: String): LocalDatabaseManager {
        localDB.edit().apply {
            putString("token", token)
            apply()
        }
        return this
    }

    fun saved(saved: Boolean): LocalDatabaseManager {
        localDB.edit().apply {
            putBoolean("saved", saved)
            apply()
        }
        return this
    }

    fun tripId(tripId: Int): LocalDatabaseManager {
        localDB.edit().apply {
            putInt("tripId", tripId)
            apply()
        }
        return this
    }

}