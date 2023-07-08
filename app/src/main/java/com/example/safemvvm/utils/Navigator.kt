package com.example.safemvvm.utils

import android.content.Intent
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity

class Navigator(private val context: AppCompatActivity) {
    private lateinit var intent: Intent
    fun <T> to(destination: Class<T>): Navigator {
        intent = Intent(context, destination)
        return this
    }

    fun andPutExtraString(key: String, value: String): Navigator {
        intent.putExtra(key, value)
        return this
    }

    fun andPutExtraInt(key: String, value: Int): Navigator {
        intent.putExtra(key, value)
        return this
    }

    fun andPutExtraParcelable(key: String, value: Parcelable): Navigator {
        intent.putExtra(key, value)
        return this
    }

    fun andClearTop() {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }

    fun andClearStack() {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun andKeepStack() {
        context.startActivity(intent)
    }
}