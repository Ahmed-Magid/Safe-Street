package com.example.safemvvm.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.safemvvm.models.MainResponse
import com.example.safemvvm.views.Login
import com.google.gson.Gson
import retrofit2.Response

class ResponseHandler(private val context: AppCompatActivity) {
    private val successfulMessages = listOf("Created Successfully", "Executed Successfully", "Deleted Successfully")
    fun <T : Any> observeResponse(
        observer: MutableLiveData<Response<MainResponse>>,
        dataType: Class<T>,
        onResponseSuccessful: (T) -> Unit,
        onResponseNotSuccessful: (String?) -> Unit,
    ) {
        observer.observe(context) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message
                if (successfulMessages.contains(responseMessage)) {
                    val data = Gson().fromJson(response.body()?.data.toString(), dataType)
                    onResponseSuccessful(data)
                } else if (responseMessage == "Authentication Error") {
                    Toast.makeText(context, "Session Expired", Toast.LENGTH_LONG).show()
                    LocalDatabaseManager(context).token("empty")
                    Navigator(context).to(Login::class.java).andClearStack()
                } else {
                    onResponseNotSuccessful(responseMessage)
                }
            } else {
                Toast.makeText(context, "Session Expired", Toast.LENGTH_LONG).show()
                LocalDatabaseManager(context).token("empty")
                Navigator(context).to(Login::class.java).andClearStack()
            }
        }
    }

    fun observeFlaskResponse(
        observer: MutableLiveData<Response<String>>,
        onResponseSuccessful: () -> Unit,
    ) {
        observer.observe(context) { response ->
            if (response.isSuccessful) {
                onResponseSuccessful()
            } else {
                Toast.makeText(context, "Error with AI model.", Toast.LENGTH_LONG).show()
                LocalDatabaseManager(context).token("empty").id(-1)
                Navigator(context).to(Login::class.java).andClearStack()
            }
        }
    }
}