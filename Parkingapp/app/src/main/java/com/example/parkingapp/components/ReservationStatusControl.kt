package com.example.parkingapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.data.api.ParkingApi
import com.example.parkingapp.data.api.RetrofitInstance
import com.example.parkingapp.components.StatusButton


@Composable
fun ReservationStatusControl(
    reservationId: Long,
    viewModel: ParkingListViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val api: ParkingApi = RetrofitInstance.create(context)
    var message by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = api.updateReservationStatus(reservationId, "ACTIVE")
                            if (response.isSuccessful) {
                                message = "ACTIVE로 변경됨"
                                viewModel.updateReservationStatusLocally(reservationId, "ACTIVE")
                            } else {
                                message = "실패: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            message = "오류 발생: ${e.localizedMessage}"
                        }
                    }
                }
            ) {
                Text("ACTIVE")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = api.updateReservationStatus(reservationId, "COMPLETED")
                            if (response.isSuccessful) {
                                message = "COMPLETED로 변경됨"
                                viewModel.updateReservationStatusLocally(reservationId, "COMPLETED")
                            } else {
                                message = "실패: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            message = "오류 발생: ${e.localizedMessage}"
                        }
                    }
                }
            ) {
                Text("COMPLETED")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message)
    }
}

