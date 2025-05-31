package com.example.parkingapp.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.data.dto.LoginRequest
import com.example.parkingapp.data.dto.RegisterRequest
import com.example.parkingapp.data.dto.RegisterResponse
import com.example.parkingapp.data.dto.UserInfoResponse
import com.example.parkingapp.data.preferences.UserPreferences
import kotlinx.coroutines.launch


class AuthViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val preferences = UserPreferences(appContext)

    val userInfo = mutableStateOf<UserInfoResponse?>(null)
    val registerState = mutableStateOf<RegisterResponse?>(null)

    fun fetchUserInfo(onFailure: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.create(appContext).getMyInfo()
                if (response.isSuccessful) {
                    userInfo.value = response.body()
                } else {
                    onFailure("유저 정보 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("오류: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val cleanUsername = username.trim()
                val cleanPassword = password.trim()
                val response = RetrofitInstance.create(appContext).login(LoginRequest(cleanUsername, cleanPassword))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        preferences.saveToken(body.token)
                        onSuccess()
                    } else {
                        onFailure("응답 없음")
                    }
                } else {
                    onFailure("로그인 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("오류: ${e.message}")
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val api = RetrofitInstance.create(appContext)
                val cleanRequest = RegisterRequest(
                    request.username.trim(),
                    request.password.trim(),
                    request.email.trim(),
                    request.phone.trim()
                )
                val response = api.register(cleanRequest)
                if (response.isSuccessful) {
                    registerState.value = response.body()
                    onSuccess()
                } else {
                    onFailure("회원가입 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onFailure("오류: ${e.message}")
            }
        }
    }

    fun logout() {
        preferences.clearToken()
        userInfo.value = null
    }
}


