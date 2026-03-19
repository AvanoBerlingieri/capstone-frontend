package capstone.safeline.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.apis.ApiServiceAuth

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val apiService: ApiServiceAuth
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}