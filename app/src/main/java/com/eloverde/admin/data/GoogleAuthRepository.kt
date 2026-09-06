package com.eloverde.admin.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.eloverde.admin.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withTimeout

class GoogleAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signIn(context: Context): Result<Unit> = runCatching {
        val activity = context.requireActivity()
        val credentialManager = CredentialManager.create(activity)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            activity.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credential = withTimeout(SIGN_IN_TIMEOUT_MILLIS) {
            credentialManager.getCredential(
                context = MutableContextWrapper(activity),
                request = request
            ).credential
        }

        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "A credencial retornada não é uma conta Google." }

        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleCredential.idToken,
            null
        )

        suspendCoroutine { continuation ->
            auth.signInWithCredential(firebaseCredential)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener(continuation::resumeWithException)
        }
    }.onFailure {
        Log.e(TAG, "Google sign-in failed", it)
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        runCatching {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        }.onFailure {
            Log.w(TAG, "Could not clear Credential Manager state", it)
        }
    }

    private fun Context.requireActivity(): Activity = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.requireActivity()
        else -> error("O login precisa ser iniciado por uma Activity.")
    }

    private companion object {
        const val TAG = "GoogleAuthRepository"
        const val SIGN_IN_TIMEOUT_MILLIS = 60_000L
    }
}
