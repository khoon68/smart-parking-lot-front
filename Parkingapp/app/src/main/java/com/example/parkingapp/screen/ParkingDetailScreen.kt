package com.example.parkingapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkingapp.ParkingLot
import com.example.parkingapp.components.TopBar

@Composable
fun ParkingDetailScreen(
    parking: ParkingLot,
    onReserveClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(title = "상세 정보", showBack = true, onBackClick = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = parking.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = "주소: ${parking.address}")
            Text(text = "요금: ${parking.pricePerHour}원/시간")
            Text(
                text = if (parking.isAvailable) "이용 가능" else "이용 불가",
                color = if (parking.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onReserveClick(parking.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = parking.isAvailable
            ) {
                Text("예약하기")
            }
        }
    }
}

