package com.example.parkingapp.data.api

import com.example.parkingapp.data.preferences.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferences.getToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
