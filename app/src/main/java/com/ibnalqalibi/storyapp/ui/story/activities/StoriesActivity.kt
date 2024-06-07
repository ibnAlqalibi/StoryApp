package com.ibnalqalibi.storyapp.ui.story.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibnalqalibi.storyapp.R
import com.ibnalqalibi.storyapp.data.ViewModelFactory
import com.ibnalqalibi.storyapp.databinding.ActivityStoriesBinding
import com.ibnalqalibi.storyapp.ui.auth.activities.LoginActivity
import com.ibnalqalibi.storyapp.ui.story.adapter.LoadingStateAdapter
import com.ibnalqalibi.storyapp.ui.story.adapter.StoryAdapter
import com.ibnalqalibi.storyapp.ui.story.viewmodels.StoriesViewModel
import kotlinx.coroutines.launch

class StoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoriesBinding
    private val viewModel by viewModels<StoriesViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun locPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!locPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        lifecycleScope.launch {
            viewModel.stories.observe(this@StoriesActivity){ stories ->
                val adapter = StoryAdapter(this@StoriesActivity)
                binding.rvHeroes.layoutManager = LinearLayoutManager(this@StoriesActivity)
                binding.rvHeroes.adapter = adapter.withLoadStateFooter(
                    footer = LoadingStateAdapter {
                        adapter.retry()
                    }
                )
                adapter.submitData(lifecycle, stories)
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_story -> {
                    startActivity(Intent(this, AddStoryActivity::class.java))
                    true
                }
                R.id.action_logout -> {
                    viewModel.deleteUserToken()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                R.id.action_map -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}