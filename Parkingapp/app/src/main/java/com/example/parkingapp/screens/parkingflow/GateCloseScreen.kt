package com.example.parkingapp.screens.parkingflow

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.components.TopBar

@Composable
fun GateCloseScreen(
    reservationId: Long,
    slotId: Long, // ✅ 추가: 서버로 보낼 슬롯 ID
    navController: NavController,
    viewModel: ParkingListViewModel,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopBar(title = "차단기 닫기", showBack = true, onBackClick = onBack) }
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
                text = "출차 완료 후 아래 버튼을 눌러 차단기를 닫아주세요",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier.size(200.dp)
            ) {
                Text("차단기 닫기", fontSize = 18.sp, color = Color.White)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.closeBarrier(
                        reservationId = reservationId,
                        slotId = slotId,
                        onSuccess = {
                            navController.navigate("list") {
                                popUpTo("list") { inclusive = true }
                            }
                        },
                        onFailure = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            navController.navigate("list") {
                                popUpTo("list") { inclusive = true }
                            }
                        }
                    )
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            },
            title = { Text("차단기 닫기") },
            text = { Text("정말로 차단기를 닫겠습니까?") }
        )
    }
}
