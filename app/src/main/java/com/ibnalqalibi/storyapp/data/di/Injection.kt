package com.ibnalqalibi.storyapp.data.di

import android.content.Context
import com.ibnalqalibi.storyapp.data.local.preferences.UserPreference
import com.ibnalqalibi.storyapp.data.local.preferences.dataStore
import com.ibnalqalibi.storyapp.data.local.repository.UserRepository
import com.ibnalqalibi.storyapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(pref, apiService)
    }
}