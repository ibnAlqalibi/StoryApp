package com.ibnalqalibi.storyapp.data.local.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.ibnalqalibi.storyapp.data.local.preferences.UserPreference
import com.ibnalqalibi.storyapp.data.remote.StoryPagingSource
import com.ibnalqalibi.storyapp.data.remote.responses.ListStoryItem
import com.ibnalqalibi.storyapp.data.remote.retrofit.ApiService

class UserRepository private constructor(private val pref: UserPreference, private val apiServices: ApiService) {
    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiServices, pref)
            }
        ).liveData
    }
    suspend fun saveUserToken(token: String) {
        pref.saveToken(token)
    }
    suspend fun deleteToken() {
        pref.deleteToken()
    }
    suspend fun getUserToken(): String {
        return pref.getUserToken()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            pref: UserPreference,
            apiServices: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(pref, apiServices)
            }.also { instance = it }
    }
}