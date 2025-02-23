package com.example.supauthppp.data.model

sealed class UserState {
    object Loading: UserState()
    object Original: UserState()
    data class Success(val message: String): UserState()
    data class Error(val message: String): UserState()
}