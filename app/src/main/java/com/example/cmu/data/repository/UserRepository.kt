package com.example.cmu.data.repository

import com.example.cmu.data.local.UserDao
import com.example.cmu.data.local.UserEntity
import com.example.cmu.data.remote.FirebaseProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(private val dao: UserDao) {

    suspend fun saveUserFromFirebase(user: FirebaseUser, customName: String? = null) {
        val entity = UserEntity(
            uid = user.uid,
            email = user.email,
            name = customName ?: user.displayName,
            photoUrl = user.photoUrl?.toString()
        )
        dao.insertUser(entity)

        val data = hashMapOf(
            "uid" to entity.uid,
            "email" to entity.email,
            "name" to entity.name,
            "photoUrl" to entity.photoUrl,
            "createdAt" to entity.createdAt
        )
        FirebaseProvider.db.collection("users")
            .document(entity.uid)
            .set(data)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.markSynced(entity.uid)
                }
            }
    }
    suspend fun getUser(uid: String): UserEntity? {
        return dao.getUserById(uid)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.insertUser(user)

        val data = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to user.name,
            "photoUrl" to user.photoUrl,
            "createdAt" to user.createdAt
        )
        FirebaseProvider.db.collection("users")
            .document(user.uid)
            .set(data)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.markSynced(user.uid)
                }
            }
    }

}
