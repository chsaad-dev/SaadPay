package com.example.saadpay.data.repository

import android.util.Log
import com.example.saadpay.data.model.Transaction
import com.example.saadpay.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.*

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

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

    fun sendMoney(
        receiverEmail: String,
        amount: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        val senderId = getCurrentUserId() ?: return

        getUserByEmail(receiverEmail) { receiverUser, error ->
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

                // Update balances
                transaction.update(senderRef, "balance", senderBalance - amount)
                transaction.update(receiverRef, "balance", receiverBalance + amount)

                // Save transaction
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

            // Save transaction
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.e("FirestoreRepository", "fetchTransactionsForCurrentUser: UID is null")
            onResult(emptyList())
            return
        }

        Log.d("FirestoreRepository", "Fetching transactions for UID: $uid")

        FirebaseFirestore.getInstance().collection("transactions")
            .whereArrayContains("participants", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("FirestoreRepository", "Fetched ${snapshot.documents.size} transactions")
                val txns = snapshot.documents.mapNotNull { doc ->
                    try {
                        val txn = doc.toObject(Transaction::class.java)
                        Log.d("FirestoreRepository", "Parsed transaction: $txn")
                        txn
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




    fun listenToCurrentUser(onUserUpdate: (User?) -> Unit): ListenerRegistration? {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return null
        val docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
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
