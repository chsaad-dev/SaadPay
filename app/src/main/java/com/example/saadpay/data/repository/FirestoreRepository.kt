package com.example.saadpay.data.repository

import android.util.Log
import com.example.saadpay.domain.model.Transaction
import com.example.saadpay.data.model.User
import com.example.saadpay.domain.model.CardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getUserByEmail(email: String, onResult: (User?, Exception?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                val user = doc?.toObject(User::class.java)?.copy(uid = doc.id)
                onResult(user, null)
            }
            .addOnFailureListener { e ->
                onResult(null, e)
            }
    }

    fun getCurrentUser(onResult: (User?, Exception?) -> Unit) {
        val uid = getCurrentUserId() ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                onResult(user, null)
            }
            .addOnFailureListener { e ->
                onResult(null, e)
            }
    }

    fun updateBalance(uid: String, newBalance: Double, onResult: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .update("balance", newBalance)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun sendMoney(receiverEmail: String, amount: Double, onResult: (Boolean, String) -> Unit) {
        val senderId = getCurrentUserId() ?: return

        getUserByEmail(receiverEmail) { receiverUser, _ ->
            if (receiverUser == null || receiverUser.uid == senderId) {
                onResult(false, "Invalid recipient")
                return@getUserByEmail
            }

            val senderRef = db.collection("users").document(senderId)
            val receiverRef = db.collection("users").document(receiverUser.uid)

            db.runTransaction { transaction ->
                val senderSnap = transaction.get(senderRef)
                val receiverSnap = transaction.get(receiverRef)

                val senderBalance = senderSnap.getDouble("balance") ?: 0.0
                val receiverBalance = receiverSnap.getDouble("balance") ?: 0.0

                if (senderBalance < amount) throw Exception("Insufficient balance")

                transaction.update(senderRef, "balance", senderBalance - amount)
                transaction.update(receiverRef, "balance", receiverBalance + amount)

                val txnId = UUID.randomUUID().toString()
                val transactionData = Transaction(
                    id = txnId,
                    senderId = senderId,
                    receiverId = receiverUser.uid,
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    type = "Send"
                )

                val txnMap = mapOf(
                    "id" to transactionData.id,
                    "senderId" to transactionData.senderId,
                    "receiverId" to transactionData.receiverId,
                    "amount" to transactionData.amount,
                    "timestamp" to transactionData.timestamp,
                    "type" to transactionData.type,
                    "participants" to listOf(senderId, receiverUser.uid)
                )

                transaction.set(db.collection("transactions").document(txnId), txnMap)
            }.addOnSuccessListener {
                onResult(true, "Success")
            }.addOnFailureListener { e ->
                onResult(false, e.message ?: "Transaction failed")
            }
        }
    }

    fun loadMoney(amount: Double, onResult: (Boolean) -> Unit) {
        val uid = getCurrentUserId() ?: return
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getDouble("balance") ?: 0.0
            val newBalance = currentBalance + amount
            transaction.update(userRef, "balance", newBalance)

            val txnId = UUID.randomUUID().toString()
            val txn = Transaction(
                id = txnId,
                senderId = uid,
                receiverId = uid,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                type = "Load"
            )

            val txnMap = mapOf(
                "id" to txn.id,
                "senderId" to txn.senderId,
                "receiverId" to txn.receiverId,
                "amount" to txn.amount,
                "timestamp" to txn.timestamp,
                "type" to txn.type,
                "participants" to listOf(uid)
            )

            transaction.set(db.collection("transactions").document(txnId), txnMap)
        }.addOnSuccessListener {
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun fetchTransactionsForCurrentUser(onResult: (List<Transaction>) -> Unit) {
        val uid = getCurrentUserId()
        if (uid == null) {
            Log.e("FirestoreRepository", "fetchTransactionsForCurrentUser: UID is null")
            onResult(emptyList())
            return
        }

        db.collection("transactions")
            .whereArrayContains("participants", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val txns = snapshot.documents.mapNotNull {
                    try {
                        it.toObject(Transaction::class.java)
                    } catch (e: Exception) {
                        Log.e("FirestoreRepository", "Error parsing transaction: ${e.message}")
                        null
                    }
                }
                onResult(txns)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Failed to fetch transactions: ${e.message}")
                onResult(emptyList())
            }
    }

    suspend fun getUserTransactions(userId: String): List<Transaction> {
        return try {
            val sentSnapshot = db.collection("transactions")
                .whereEqualTo("senderId", userId)
                .get()
                .await()

            val receivedSnapshot = db.collection("transactions")
                .whereEqualTo("receiverId", userId)
                .get()
                .await()

            val sentTransactions = sentSnapshot.toObjects(Transaction::class.java)
            val receivedTransactions = receivedSnapshot.toObjects(Transaction::class.java)

            (sentTransactions + receivedTransactions).sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error in getUserTransactions: ${e.message}")
            emptyList()
        }
    }

    fun getTransactionHistory(callback: (List<Transaction>, Exception?) -> Unit) {
        val uid = getCurrentUserId()
        if (uid == null) {
            callback(emptyList(), Exception("User not logged in"))
            return
        }

        db.collection("transactions")
            .whereArrayContains("participants", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val txns = snapshot.documents.mapNotNull {
                    try {
                        it.toObject(Transaction::class.java)
                    } catch (e: Exception) {
                        Log.e("FirestoreRepository", "Error parsing transaction: ${e.message}")
                        null
                    }
                }
                callback(txns, null)
            }
            .addOnFailureListener { e ->
                callback(emptyList(), e)
            }
    }

    // ------------------ CARD METHODS --------------------

    fun getUserCard(userId: String, onResult: (CardModel?) -> Unit) {
        db.collection("cards")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val card = doc.toObject(CardModel::class.java)
                onResult(card)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun updateUserCard(userId: String, card: CardModel, onResult: (Boolean) -> Unit) {
        db.collection("cards").document(userId)
            .set(card)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.e("FirestoreRepository", "Error updating card: ${it.message}")
                onResult(false)
            }
    }

    // ------------------ USER LISTENER --------------------

    fun listenToCurrentUser(onUserUpdate: (User?) -> Unit): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null
        val docRef = db.collection("users").document(currentUser.uid)
        return docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                onUserUpdate(user)
            } else {
                onUserUpdate(null)
            }
        }
    }

    fun removeListener(listenerRegistration: ListenerRegistration?) {
        listenerRegistration?.remove()
    }
}
