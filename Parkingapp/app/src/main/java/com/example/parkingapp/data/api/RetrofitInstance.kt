package com.example.parkingapp.data.api

import android.content.Context
import com.example.parkingapp.data.preferences.UserPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
//    private const val BASE_URL = "http://54.180.66.26:8080/"
    private const val BASE_URL = "http://10.0.2.2:8080/"
    fun create(context: Context): ParkingApi {
        val preferences = UserPreferences(context)
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(preferences))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ParkingApi::class.java)
    }
}
