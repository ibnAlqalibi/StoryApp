package com.ibnalqalibi.storyapp.ui.auth.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ibnalqalibi.storyapp.data.ViewModelFactory
import com.ibnalqalibi.storyapp.databinding.ActivityLoginBinding
import com.ibnalqalibi.storyapp.ui.auth.viewmodels.AuthViewModel
import com.ibnalqalibi.storyapp.ui.story.activities.StoriesActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<AuthViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        startLogin()
    }

    private fun startLogin() {
        binding.btnLogin.setOnClickListener {
            showLoading(true)
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (validateInput(email, password)) {
                lifecycleScope.launch {
                    viewModel.login(email, password)
                    viewModel.message.observe(this@LoginActivity) { showToast(it) }
                    viewModel.isLoading.observe(this@LoginActivity) { showLoading(it) }
                    viewModel.loginResult.observe(this@LoginActivity) {
                        viewModel.saveUserToken(it.loginResult.token)
                        Log.e("Login", it.loginResult.token)
                        val intent = Intent(this@LoginActivity, StoriesActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email dan password harus diisi")
            showLoading(false)
            return false
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}