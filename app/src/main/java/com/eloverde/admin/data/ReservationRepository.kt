package com.eloverde.admin.data

import com.eloverde.admin.domain.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ReservationRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    fun observe(onResult: (List<Reservation>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration = db.collection("reservationIntents").addSnapshotListener { snapshot, error ->
        if (error != null) onError(error) else onResult(snapshot?.documents.orEmpty().map { d -> Reservation(d.id, d.getString("name").orEmpty(), d.getString("phone").orEmpty(), d.getString("email").orEmpty(), d.getString("date").orEmpty(), d.getString("notes").orEmpty(), d.getString("status").orEmpty()) }.sortedBy { it.date })
    }
    fun updateStatus(id: String, status: String) = db.collection("reservationIntents").document(id).update("status", status, "updatedBy", "mobile-admin")
}
