package com.eloverde.admin.domain

import java.time.Instant

data class Reservation(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val date: String,
    val notes: String,
    val status: ReservationStatus,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val updatedBy: String
)
