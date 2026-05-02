package com.medvision.ai.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.medvision.ai.data.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HistoryRepository(
    private val firestore: FirebaseFirestore?,
    private val authRepository: AuthRepository
) {
    private val cache = MutableStateFlow<List<HistoryItem>>(emptyList())

    fun observeHistory(): Flow<List<HistoryItem>> = cache.asStateFlow()

    suspend fun syncRemoteHistory() {
        val user = authRepository.currentUser.value ?: return
        runCatching {
            val snapshot = firestore?.collection("users")
                ?.document(user.uid)
                ?.collection("history")
                ?.get()
                ?.await()
                ?: return
            cache.value = snapshot.documents.mapNotNull { doc ->
                doc.toObject(HistoryItem::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.timestamp }
        }
    }

    suspend fun addHistory(item: HistoryItem) {
        val record = item.copy(id = item.id.ifBlank { UUID.randomUUID().toString() })
        cache.update { (it + record).sortedByDescending { entry -> entry.timestamp } }

        val user = authRepository.currentUser.value ?: return
        runCatching {
            firestore?.collection("users")
                ?.document(user.uid)
                ?.collection("history")
                ?.document(record.id)
                ?.set(record)
                ?.await()
        }
    }
}
