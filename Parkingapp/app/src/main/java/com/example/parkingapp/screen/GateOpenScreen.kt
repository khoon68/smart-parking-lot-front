package com.example.parkingapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parkingapp.ParkingListViewModel
import com.example.parkingapp.components.TopBar

@Composable
fun GateOpenScreen(
    reservationId: Int,
    navController: NavController,
    viewModel: ParkingListViewModel,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(title = "차단기 제어", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "주차 준비가 완료된 상태에서 진행하세요",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4)),
                modifier = Modifier.size(200.dp)
            ) {
                Text("차단기 열기", fontSize = 18.sp, color = Color.White)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.markReservationStarted(reservationId) // ✅ 상태 업데이트
                    navController.navigate("list") {
                        popUpTo("list") { inclusive = true }
                    }
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            },
            title = { Text("차단기 열기") },
            text = { Text("정말로 차단기를 열겠습니까?") }
        )
    }
}
