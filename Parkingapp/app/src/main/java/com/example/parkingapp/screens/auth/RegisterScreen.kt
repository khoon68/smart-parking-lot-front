package com.example.parkingapp.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.data.dto.RegisterRequest
import com.example.parkingapp.viewmodel.AuthViewModel


@Composable
fun RegisterScreen(viewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(Modifier.padding(24.dp)) {
        Text("회원가입", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("아이디") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") })
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("전화번호") })

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.register(
                RegisterRequest(username, password, email, phone),
                onSuccess = {
                    Toast.makeText(context, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true } // ← 스택 정리
                    }
                },
                onFailure = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }) {
            Text("회원가입")
        }
    }
}