package com.example.parkingapp.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.parkingapp.screens.detail.ParkingItem
import com.example.parkingapp.viewmodel.ParkingListViewModel
import com.example.parkingapp.data.model.ParkingLot

@Composable

fun ParkingListScreen(
    viewModel: ParkingListViewModel,
    navController: NavController,
    onItemClick: (ParkingLot) -> Unit
) {

    val parkingList = viewModel.parkingList.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(parkingList.value) { parking ->
                ParkingItem(parking = parking) {
                    onItemClick(parking)
                }
            }
        }
        Button(
            onClick = { navController.navigate("mypage") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("마이페이지로 가기")
        }
    }
}

