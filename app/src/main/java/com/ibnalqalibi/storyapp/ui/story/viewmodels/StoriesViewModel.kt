package com.ibnalqalibi.storyapp.ui.story.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.ibnalqalibi.storyapp.data.local.repository.UserRepository
import com.ibnalqalibi.storyapp.data.remote.responses.ListStoryItem
import com.ibnalqalibi.storyapp.data.remote.responses.RegisterResponse
import com.ibnalqalibi.storyapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoriesViewModel (private val repository: UserRepository) : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _storiesMap = MutableLiveData<List<ListStoryItem>>()
    val storiesMap: LiveData<List<ListStoryItem>> = _storiesMap

    val stories: LiveData<PagingData<ListStoryItem>> = repository.getStories().cachedIn(viewModelScope)

    suspend fun stories(page: Int? = null, size: Int? = null, location: Int? = null){
        try {
            val token = repository.getUserToken()
            val response = ApiConfig.getApiService().stories(page, size, location, "Bearer $token")
            if (response.isSuccessful) {
                _storiesMap.postValue(response.body()?.listStory)
                Log.e("Products", "Isi: ${response.body()?.listStory}")
            } else {
                Log.e("Products", "Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("Products", "Exception: ${e.message.toString()}")
        }
    }

    suspend fun addStory(photo: MultipartBody.Part, description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null) {
        try {
            val token = repository.getUserToken()
            val apiService = ApiConfig.getApiService()
            val successResponse = apiService.uploadStory(photo, description, lat, lon, "Bearer $token")
            _message.value = successResponse.message
            _isLoading.value = false
            _isSuccess.value = true
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
            _message.value = errorResponse.message
            _isLoading.value = false
        }
    }

    fun deleteUserToken() {
        viewModelScope.launch {
            repository.deleteToken()
        }
    }
}