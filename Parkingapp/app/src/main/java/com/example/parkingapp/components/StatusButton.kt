package com.example.parkingapp.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.parkingapp.data.api.ParkingApi
import com.example.parkingapp.viewmodel.ParkingListViewModel
import kotlinx.coroutines.launch

@Composable
public fun StatusButton(
    status: String,
    reservationId: Long,
    api: ParkingApi,
    viewModel: ParkingListViewModel,
    onMessage: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        coroutineScope.launch {
            try {
                val response = api.updateReservationStatus(reservationId, status)
                if (response.isSuccessful) {
                    viewModel.updateReservationStatusLocally(reservationId, status)
                    onMessage("$status 로 변경됨")
                } else {
                    onMessage("실패: ${response.code()}")
                }
            } catch (e: Exception) {
                onMessage("오류 발생: ${e.localizedMessage}")
            }
        }
    }) {
        Text(status)
    }
}