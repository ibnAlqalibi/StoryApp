package com.ibnalqalibi.storyapp.ui.story.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.ibnalqalibi.storyapp.data.remote.responses.ListStoryItem
import com.ibnalqalibi.storyapp.databinding.ActivityStoryDetailBinding

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupData()
    }

    private fun setupData() {
        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY) as ListStoryItem
        Glide.with(applicationContext)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}