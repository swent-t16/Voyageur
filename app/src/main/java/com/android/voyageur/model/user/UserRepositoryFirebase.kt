package com.android.voyageur.model.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepositoryFirebase(private val db: FirebaseFirestore) : UserRepository {
    private val collectionPath = "users"

    override fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                try {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User not found"))
                    }
                } catch (e: Exception) {
                    Log.e("UserRepositoryFirebase", "Error parsing user: ", e)
                    onFailure(e)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryFirebase", "Error getting user: ", exception)
                onFailure(exception)
            }
    }

    override fun init(onSuccess: () -> Unit) {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryFirebase", "Error initializing repository: ", exception)
            }
    }

    override fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryFirebase", "Error creating user: ", exception)
                onFailure(exception)
            }
    }

    override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryFirebase", "Error updating user: ", exception)
                onFailure(exception)
            }
    }

    override fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryFirebase", "Error deleting user: ", exception)
                onFailure(exception)
            }
    }

    fun getNewUserId(): String {
        return db.collection(collectionPath).document().id
    }
    companion object {
        fun create(): UserRepositoryFirebase {
            val db = FirebaseFirestore.getInstance()
            return UserRepositoryFirebase(db)
        }
    }

}