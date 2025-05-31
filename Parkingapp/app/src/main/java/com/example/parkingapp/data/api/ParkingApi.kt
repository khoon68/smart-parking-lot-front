package com.example.parkingapp.data.api

import com.example.parkingapp.data.dto.BarrierOpenRequest
import com.example.parkingapp.data.dto.LoginRequest
import com.example.parkingapp.data.dto.LoginResponse
import com.example.parkingapp.data.dto.ParkingSlotDTO
import com.example.parkingapp.data.dto.RegisterRequest
import com.example.parkingapp.data.dto.RegisterResponse
import com.example.parkingapp.data.dto.ReservationRequest
import com.example.parkingapp.data.dto.ReservationResponse
import com.example.parkingapp.data.dto.UserInfoResponse
import com.example.parkingapp.data.model.ParkingLot
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ParkingApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMyInfo(): Response<UserInfoResponse>

    @GET("parking-lots")
    suspend fun getParkingLots(): List<ParkingLot>

    @POST("reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest): Response<ReservationResponse>

    @GET("parking-lots/{id}/available-slots")
    suspend fun getAvailableSlots(
        @Path("id") parkingLotId: Long,
        @Query("date") date: String,
        @Query("timeSlots") timeSlots: List<String>
    ): List<ParkingSlotDTO>

    @GET("reservations/my")
    suspend fun getMyReservations(): List<ReservationResponse>

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(
        @Path("id") reservationId: Long): Response<Unit>

    @POST("reservations/barrier/open")
    suspend fun openBarrier(
        @Body request: BarrierOpenRequest): Response<String>

    @POST("reservations/barrier/close")
    suspend fun closeBarrier(@Body request: BarrierOpenRequest): Response<String>
}
