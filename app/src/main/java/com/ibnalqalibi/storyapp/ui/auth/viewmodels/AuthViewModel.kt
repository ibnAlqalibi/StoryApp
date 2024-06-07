package com.ibnalqalibi.storyapp.ui.auth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ibnalqalibi.storyapp.data.local.repository.UserRepository
import com.ibnalqalibi.storyapp.data.remote.responses.LoginResponse
import com.ibnalqalibi.storyapp.data.remote.responses.RegisterResponse
import com.ibnalqalibi.storyapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel (private val repository: UserRepository) : ViewModel() {
    private val _regisResult = MutableLiveData<RegisterResponse>()
    val regisResult: LiveData<RegisterResponse> = _regisResult

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> = _loginResult

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    suspend fun register(name: String, email: String, password: String) {
        try {
            val apiService = ApiConfig.getApiService()
            val successResponse = apiService.register(name, email, password)
            _message.value = successResponse.message
            _regisResult.value = successResponse
            _isLoading.value = false
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
            _message.value = errorResponse.message
            _isLoading.value = false
        }
    }

    suspend fun login(email: String, password: String) {
        try {
            val apiService = ApiConfig.getApiService()
            val successResponse = apiService.login(email, password)
            _message.value = successResponse.message
            _loginResult.value = successResponse
            _isLoading.value = false
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
            _message.value = errorResponse.message
            _isLoading.value = false
        }
    }

    fun saveUserToken(token: String) {
        viewModelScope.launch {
            repository.saveUserToken(token)
        }
    }
}