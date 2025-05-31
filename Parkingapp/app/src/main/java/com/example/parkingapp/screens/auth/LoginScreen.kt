package com.example.parkingapp.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.padding(24.dp)) {
        Text(text = "로그인", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        // 아이디 입력
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 로그인 버튼
        Button(
            onClick = {
                viewModel.login(
                    username,
                    password,
                    onSuccess = {
                        viewModel.fetchUserInfo()
                        // 로그인 성공 시 목록 화면으로 이동하고 로그인 스택 제거
                        navController.navigate("list") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onFailure = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }

        Spacer(Modifier.height(8.dp))

        // 회원가입으로 이동하는 링크
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("회원가입이 필요하신가요?")
        }
    }
}
