package com.example.parkingapp.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.parkingapp.ParkingListViewModel
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.ui.theme.formatContinuousTime

@Composable
fun MyPageScreen(
    viewModel: ParkingListViewModel,
    onBack: () -> Unit,
    navController: NavController
) {
    val reservations = viewModel.reservationHistory.collectAsState()

    Scaffold(
        topBar = { TopBar(title = "예약 내역", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(reservations.value) { reservation ->
                val isParking = reservation.isOngoing
                val statusLabel = if (isParking) "주차중" else "주차가능"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("주차장: ${reservation.parking.name}", style = MaterialTheme.typography.titleMedium)
                        Text("주소: ${reservation.parking.address}", style = MaterialTheme.typography.bodyMedium)
                        Text("시간: ${formatContinuousTime(reservation.timeSlots)}", style = MaterialTheme.typography.bodyMedium)
                        Text("상태: $statusLabel", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            Button(
                                onClick = { viewModel.cancelReservation(reservation.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = !isParking // ✅ 주차중이면 비활성화
                            ) {
                                Text("예약 취소")
                            }

                            if (!isParking) {
                                Button(
                                    onClick = {
                                        navController.navigate("notice/${reservation.id}")
                                        // 주차 시작 여부는 차단기 열기 화면에서 처리됨
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("주차 시작")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        navController.navigate("exitNotice/${reservation.id}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("주차 종료")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}