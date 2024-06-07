package com.ibnalqalibi.storyapp.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ibnalqalibi.storyapp.data.local.preferences.UserPreference
import com.ibnalqalibi.storyapp.data.remote.responses.ListStoryItem
import com.ibnalqalibi.storyapp.data.remote.retrofit.ApiService

class StoryPagingSource(private val apiService: ApiService, private val pref: UserPreference) : PagingSource<Int, ListStoryItem>() {
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = pref.getUserToken()
            val responseData = apiService.storiesPaging(position, params.loadSize, 0, "Bearer $token")
            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

}