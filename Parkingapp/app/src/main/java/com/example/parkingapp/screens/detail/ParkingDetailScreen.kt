package com.example.parkingapp.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.parkingapp.R
import com.example.parkingapp.components.TopBar
import com.example.parkingapp.data.model.ParkingLot

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
            // ✅ 미리보기 이미지 추가 (위에만 삽입)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(parking.imageUrl) // 서버에서 받는 이미지 url
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.default_parking_image),
                error = painterResource(R.drawable.default_parking_image),
                contentDescription = "주차장 미리보기",
                contentScale = ContentScale.Crop, //비율에 맞게 보여줌
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // 💡 16:9에 맞는 비율 설정
            )
            Text(text = parking.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = "거리: ${parking.distance}m")
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
