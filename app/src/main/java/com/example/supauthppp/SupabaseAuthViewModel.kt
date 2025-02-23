package com.example.supauthppp

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supauthppp.data.model.UserState
import com.example.supauthppp.data.network.SupabaseClient.client
import com.example.supauthppp.utils.SharedPreferenceHelper
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class SupabaseAuthViewModel : ViewModel() {

    private val _userState = mutableStateOf<UserState>(UserState.Original)
    val userState = _userState

    private lateinit var sharedPref: SharedPreferenceHelper

    fun updateUserState(state: UserState){
        _userState.value = state
    }

    // Initialize SharedPreferences
    private fun initSharedPref(context: Context) {
        if (!::sharedPref.isInitialized) {
            sharedPref = SharedPreferenceHelper(context)
        }
    }

    // Common function to save token
    private fun saveToken(context: Context) {
        viewModelScope.launch {
            val accessToken = client.gotrue.currentAccessTokenOrNull() ?: ""
            initSharedPref(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    // Common function to retrieve token
    private fun getToken(context: Context): String? {
        initSharedPref(context)
        return sharedPref.getStringData("accessToken")
    }

    // Sign up with email and password
    fun signUp(context: Context, userEmail: String, userPassword: String) {
        authenticateUser(context, userEmail, userPassword, isSignUp = true)
    }

    // Log in with email and password
    fun login(context: Context, userEmail: String, userPassword: String) {
        authenticateUser(context, userEmail, userPassword, isSignUp = false)
    }

    // Common function for sign-up and login
    private fun authenticateUser(
        context: Context,
        userEmail: String,
        userPassword: String,
        isSignUp: Boolean
    ) {
        viewModelScope.launch {
            try {
                updateUserState(UserState.Loading)
                if (isSignUp) {
                    client.gotrue.signUpWith(Email) {
                        email = userEmail
                        password = userPassword
                    }
                } else {
                    client.gotrue.loginWith(Email) {
                        email = userEmail
                        password = userPassword
                    }
                }
                saveToken(context)
                updateUserState(UserState.Success(""))
            } catch (e: Exception) {
                updateUserState(UserState.Error(e.message ?: "An error occurred"))
            }
        }
    }

    // Log out the user
    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                updateUserState(UserState.Loading)
                client.gotrue.logout()
                saveToken(context) // Clear the token
                updateUserState(UserState.Original) // Reset to the original state
            } catch (e: Exception) {
                updateUserState(UserState.Error(e.message ?: "Logout failed"))
            }
        }
    }

    fun isUserLoggedIn(context: Context) {
        viewModelScope.launch {
            try {
                updateUserState(UserState.Loading)
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    updateUserState(UserState.Original) // No token, reset to original state
                } else {
                    client.gotrue.retrieveUser(token)
                    client.gotrue.refreshCurrentSession()
                    saveToken(context)
                    updateUserState(UserState.Success("User is logged in")) // Set success state
                }
            } catch (e: RestException) {
                updateUserState(UserState.Error(e.error))
            }
        }
    }
}