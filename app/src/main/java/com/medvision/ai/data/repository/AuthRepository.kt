package com.medvision.ai.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.medvision.ai.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth?
) {
    private val mockUser = MutableStateFlow<UserProfile?>(firebaseAuth?.currentUser?.toUserProfile())

    val currentUser: StateFlow<UserProfile?> = mockUser.asStateFlow()

    init {
        firebaseAuth?.addAuthStateListener { auth ->
            mockUser.value = auth.currentUser?.toUserProfile()
        }
    }

    suspend fun login(email: String, password: String): Result<UserProfile> = firebaseResult {
        if (firebaseAuth == null) {
            UserProfile(uid = "offline_user", name = email.substringBefore("@"), email = email)
                .also { mockUser.value = it }
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).await().user?.toUserProfile()
                ?: error("Unable to sign in.")
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<UserProfile> = firebaseResult {
        if (firebaseAuth == null) {
            UserProfile(uid = "offline_user", name = name, email = email).also { mockUser.value = it }
        } else {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
                ?: error("Unable to create account.")
            user.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name).build()
            ).await()
            UserProfile(uid = user.uid, name = name, email = email)
        }
    }

    fun logout() {
        firebaseAuth?.signOut()
        if (firebaseAuth == null) mockUser.value = null
    }

    private suspend fun <T> firebaseResult(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(exception.toFriendlyAuthMessage(), exception))
        }
    }

    private fun Exception.toFriendlyAuthMessage(): String {
        val authErrorCode = (this as? FirebaseAuthException)?.errorCode.orEmpty()
        val rawMessage = message.orEmpty()
        return when {
            authErrorCode.contains("CONFIGURATION_NOT_FOUND") ||
                rawMessage.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Firebase Authentication is not enabled for this project. In Firebase Console, open Authentication > Sign-in method and enable Email/Password, then reinstall the app."
            rawMessage.isNotBlank() -> rawMessage
            else -> "Authentication failed. Please try again."
        }
    }

    private fun com.google.firebase.auth.FirebaseUser.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            name = displayName ?: email?.substringBefore("@") ?: "User",
            email = email.orEmpty()
        )
    }
}
