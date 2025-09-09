package com.example.cmu.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cmu.data.local.UserEntity
import com.example.cmu.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun saveUser(user: FirebaseUser, customName: String? = null) {
        viewModelScope.launch {
            repository.saveUserFromFirebase(user, customName)
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    suspend fun getUser(uid: String): UserEntity? {
        return repository.getUser(uid)
    }
}


