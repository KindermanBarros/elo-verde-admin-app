package com.eloverde.admin

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AdminDeviceRegistration {
    private const val TAG = "AdminDeviceRegistration"
    private const val COLLECTION = "adminDeviceTokens"

    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(::register)
            .addOnFailureListener { Log.w(TAG, "Could not get FCM token", it) }
    }

    fun register(token: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection(COLLECTION).document(tokenId(token))
            .set(mapOf("token" to token, "uid" to user.uid, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnFailureListener { Log.w(TAG, "Could not register FCM token", it) }
    }

    suspend fun unregisterCurrentToken() {
        val token = suspendCoroutine<String?> { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        } ?: return
        suspendCoroutine<Unit> { continuation ->
            FirebaseFirestore.getInstance().collection(COLLECTION).document(tokenId(token)).delete()
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) Log.w(TAG, "Could not unregister FCM token", task.exception)
                    continuation.resume(Unit)
                }
        }
    }

    private fun tokenId(token: String): String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
