package com.eloverde.admin.domain

data class NewReservation(
    val name: String,
    val email: String,
    val phone: String,
    val date: String,
    val notes: String,
    val status: ReservationStatus
)
