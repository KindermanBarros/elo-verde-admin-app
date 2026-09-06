package com.eloverde.admin.data

import com.eloverde.admin.domain.NewReservation
import com.eloverde.admin.domain.Reservation
import com.eloverde.admin.domain.ReservationStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
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

    fun create(
        reservation: NewReservation,
        createdBy: String
    ): Task<DocumentReference> {
        val reservationRef = db.collection(COLLECTION).document()
        val data = mapOf(
                "name" to reservation.name.trim(),
                "email" to reservation.email.trim(),
                "phone" to reservation.phone.trim(),
                "date" to reservation.date,
                "notes" to reservation.notes.trim(),
                "status" to reservation.status.wireValue,
                "updatedBy" to createdBy,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!reservation.status.blocksDate) {
            return reservationRef.set(data).continueWith { reservationRef }
        }
        val lockRef = db.collection(LOCKS).document(reservation.date)
        return db.runTransaction { transaction ->
            val lock = transaction.get(lockRef)
            if (lock.exists()) throw IllegalStateException("Esta data já possui uma reserva confirmada.")
            transaction.set(lockRef, lockData(reservationRef.id, reservation.date, reservation.status, createdBy))
            transaction.set(reservationRef, data)
            reservationRef
        }
    }

    fun updateStatus(
        id: String,
        status: ReservationStatus,
        updatedBy: String
    ): Task<Void> {
        val reservationRef = db.collection(COLLECTION).document(id)
        return db.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef)
            if (!reservation.exists()) throw IllegalStateException("Reserva não encontrada.")
            val date = reservation.getString("date").orEmpty()
            val currentStatus = ReservationStatus.from(reservation.getString("status"))
            val lockRef = db.collection(LOCKS).document(date)
            val lock = transaction.get(lockRef)

            if (status.blocksDate) {
                val owner = lock.getString("reservationId")
                if (lock.exists() && owner != id) {
                    throw IllegalStateException("Esta data já está reservada ou quitada.")
                }
                transaction.set(lockRef, lockData(id, date, status, updatedBy))
            } else if (currentStatus.blocksDate && lock.getString("reservationId") == id) {
                transaction.delete(lockRef)
            }
            transaction.update(reservationRef, mapOf(
                "status" to status.wireValue,
                "updatedBy" to updatedBy,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            null
        }
    }

    fun remove(id: String): Task<Void> {
        val reservationRef = db.collection(COLLECTION).document(id)
        return db.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef)
            if (reservation.exists()) {
                val date = reservation.getString("date").orEmpty()
                val lockRef = db.collection(LOCKS).document(date)
                val lock = transaction.get(lockRef)
                if (lock.getString("reservationId") == id) transaction.delete(lockRef)
                transaction.delete(reservationRef)
            }
            null
        }
    }

    private fun lockData(id: String, date: String, status: ReservationStatus, updatedBy: String) =
        mapOf("reservationId" to id, "date" to date, "status" to status.wireValue,
            "updatedBy" to updatedBy, "updatedAt" to FieldValue.serverTimestamp())

    private companion object {
        const val COLLECTION = "reservationIntents"
        const val LOCKS = "reservationDateLocks"
    }
}
