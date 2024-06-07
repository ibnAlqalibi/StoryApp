package com.ibnalqalibi.storyapp

import com.ibnalqalibi.storyapp.data.remote.responses.ListStoryItem

object DataDummy {

    fun generateDummyStoriesResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "Url $i",
                "createdAt $i",
                "author $i",
                "description $i",
                4.0,
                "id $i",
                324.7,
            )
            items.add(story)
        }
        return items
    }
}