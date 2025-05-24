package com.example.parkingapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkingapp.Reservation
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.ui.theme.formatContinuousTime


@Composable
fun PaymentScreen(
    reservation: Reservation,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(title = "결제", showBack = true, onBackClick = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("주차장: ${reservation.parking.name}", style = MaterialTheme.typography.titleMedium)
            Text("주소: ${reservation.parking.address}")
            Text("예약 시간: ${formatContinuousTime(reservation.timeSlots)}")
            Text("총 요금: ${reservation.totalPrice}원")

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("결제하기")
            }
        }
    }
}

