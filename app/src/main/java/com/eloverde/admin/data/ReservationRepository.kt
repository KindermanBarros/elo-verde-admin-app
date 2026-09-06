package com.eloverde.admin.data

import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ReservationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observe(
        onResult: (List<Reservation>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration =
        db.collection(COLLECTION)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val reservations = snapshot?.documents.orEmpty().map { document ->
                    Reservation(
                        id = document.id,
                        name = document.getString("name").orEmpty(),
                        phone = document.getString("phone").orEmpty(),
                        email = document.getString("email").orEmpty(),
                        date = document.getString("date").orEmpty(),
                        notes = document.getString("notes").orEmpty(),
                        status = ReservationStatus.from(document.getString("status")),
                        createdAt = document.getTimestamp("createdAt")?.toDate()?.toInstant(),
                        updatedAt = document.getTimestamp("updatedAt")?.toDate()?.toInstant(),
                        updatedBy = document.getString("updatedBy").orEmpty()
                    )
                }
                onResult(reservations)
            }

    fun updateStatus(
        id: String,
        status: ReservationStatus,
        updatedBy: String
    ): Task<Void> =
        db.collection(COLLECTION).document(id).update(
            mapOf(
                "status" to status.wireValue,
                "updatedBy" to updatedBy,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

    private companion object {
        const val COLLECTION = "reservationIntents"
    }
}
