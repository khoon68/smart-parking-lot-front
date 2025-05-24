package com.example.parkingapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkingapp.components.TopBar

@Composable
fun StartParkingScreen(
    reservationId: Int,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopBar(title = "일회용 차단기 개폐 키 발급", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("예약 번호: #$reservationId", style = MaterialTheme.typography.titleMedium)

            // 예시 키 출력 (실제 키가 있다면 서버 통신 등과 연결)
            Text("발급된 주차 키:", style = MaterialTheme.typography.bodyLarge)
            Text("KEY-${reservationId.toString().padStart(5, '0')}", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("주의사항 확인 후 계속")
            }
        }
    }
}
