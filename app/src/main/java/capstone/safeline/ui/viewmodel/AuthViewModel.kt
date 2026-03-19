package capstone.safeline.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.dto.LoginRequest
import capstone.safeline.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(
    private val repository: AuthRepository,
    private val apiServiceAuth: ApiServiceAuth
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> =
        repository.isLoggedIn
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )
    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiServiceAuth.login(LoginRequest(usernameOrEmail, password))

                println("RESPONSE CODE: ${response.code()}")
                println("RESPONSE BODY: ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body == null) {
                        println("Body is NULL")
                        return@launch
                    }

                    println("TOKEN: ${body.token}")

                    if (body.token.isNotBlank()) {
                        repository.saveToken(body.token)
                    } else {
                        println("Token is blank")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun register(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.register(username, email, password)
            onResult(success)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}