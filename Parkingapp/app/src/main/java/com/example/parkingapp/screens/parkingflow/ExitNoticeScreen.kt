package com.example.parkingapp.screens.parkingflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkingapp.components.TopBar

@Composable
fun ExitNoticeScreen(
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopBar(title = "출차 전 주의사항", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("출차 전 다음 사항을 반드시 확인하세요:", style = MaterialTheme.typography.titleMedium)

            Text("1. 남은 시간 내에 출차를 완료해 주세요.")
            Text("2. 차량 주변에 장애물이 없는지 확인하세요.")
            Text("3. 출차 후에는 예약이 자동 종료됩니다.")
            Text("4. 기타 이상 발생 시 고객센터에 연락 주세요.")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("차단기 닫기 화면으로 이동")
            }
        }
    }
}