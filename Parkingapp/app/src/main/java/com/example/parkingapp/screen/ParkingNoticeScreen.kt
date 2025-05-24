package com.example.parkingapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkingapp.components.TopBar

@Composable
fun ParkingNoticeScreen(
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopBar(title = "주차 시 주의사항", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("다음 주의사항을 반드시 확인하세요:", style = MaterialTheme.typography.titleMedium)

            Text("1. 주차장 이용 시간 종료 5분 전까지 출차를 완료해 주세요.")
            Text("2. 반드시 입차 준비가 완료된 상태에서 진행하세요.")
            Text("3. 입차 시에는 반드시 '차단기 열기' 버튼을 이용해 주세요.")
            Text("4. 기타 이상 발생 시 고객센터에 연락 주세요.")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth()
                )
            {
                Text("차단기 제어 화면으로 이동")
            }
        }
    }
}
