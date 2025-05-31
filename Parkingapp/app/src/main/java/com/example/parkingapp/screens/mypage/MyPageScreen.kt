package com.example.parkingapp.screens.mypage

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.ui.theme.formatContinuousTime
import com.example.parkingapp.viewmodel.AuthViewModel


@Composable
fun MyPageScreen(
    viewModel: ParkingListViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.fetchMyReservations()
    }

    val context = LocalContext.current
    val reservations = viewModel.reservationHistory.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedReservationId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = { TopBar(title = "예약 내역", showBack = true, onBackClick = onBack) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // ✅ 로그아웃 버튼
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("list") { inclusive = true }
                    }
                    Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text("로그아웃")
            }

            // ✅ 예약 리스트
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
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
                            Text("주소: ${reservation.parking.distance}", style = MaterialTheme.typography.bodyMedium)
                            Text("시간: ${formatContinuousTime(reservation.timeSlots)}", style = MaterialTheme.typography.bodyMedium)
                            Text("상태: $statusLabel", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                            ) {
                                Button(onClick = {
                                    viewModel.cancelReservationFromServer(
                                        reservationId = reservation.id,
                                        onSuccess = {
                                            Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }) {
                                    Text("예약 취소")
                                }

                                if (!isParking) {
                                    Button(
                                        onClick = {
                                            navController.navigate("notice/${reservation.id}/${reservation.slotId}")
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

        // ✅ 예약 취소 다이얼로그
        if (showDialog && selectedReservationId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("예약 취소") },
                text = { Text("이 예약을 정말 취소하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancelReservation(selectedReservationId!!)
                        showDialog = false
                        selectedReservationId = null
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedReservationId = null
                    }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
