package com.ibnalqalibi.storyapp.ui.auth.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.ibnalqalibi.storyapp.R
import com.ibnalqalibi.storyapp.data.local.preferences.UserPreference
import com.ibnalqalibi.storyapp.ui.story.activities.StoriesActivity
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userPreference = UserPreference.getInstance(dataStore)
        getToken()
    }

    private fun getToken() {
        lifecycleScope.launch {
            try {
                val token = userPreference.getUserToken()
                if (token == ""){
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                    finish()
                }else{
                    val intent = Intent(this@SplashActivity, StoriesActivity::class.java)
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                    finish()
                }
            } catch (e: Exception) {
                Log.e("MyActivity", "Error getting token", e)
            }
        }
    }
}