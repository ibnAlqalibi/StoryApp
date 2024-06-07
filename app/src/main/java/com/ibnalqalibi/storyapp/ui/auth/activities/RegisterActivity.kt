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
import com.ibnalqalibi.storyapp.databinding.ActivityRegisterBinding
import com.ibnalqalibi.storyapp.ui.auth.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<AuthViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startRegister()
    }

    private fun startRegister() {
        binding.btnRegister.setOnClickListener {
            showLoading(true)
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (validateInput(name, email, password)) {
                lifecycleScope.launch {
                    viewModel.register(name, email, password)
                    viewModel.message.observe(this@RegisterActivity) { showToast(it) }
                    viewModel.isLoading.observe(this@RegisterActivity) { showLoading(it) }
                    viewModel.regisResult.observe(this@RegisterActivity) {
                        Log.e("Register", it.message)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (email.isEmpty() ||  name.isEmpty() || password.isEmpty()) {
            showToast("nama, email dan password harus diisi")
            showLoading(false)
            return false
        }else if (password.length < 8) {
            showToast("password minimal 8 karakter")
            showLoading(false)
            return false
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}