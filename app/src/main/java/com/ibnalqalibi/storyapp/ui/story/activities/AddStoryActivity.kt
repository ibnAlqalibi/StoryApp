package com.ibnalqalibi.storyapp.ui.story.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ibnalqalibi.storyapp.R
import com.ibnalqalibi.storyapp.data.ViewModelFactory
import com.ibnalqalibi.storyapp.databinding.ActivityAddStoryBinding
import com.ibnalqalibi.storyapp.ui.story.viewmodels.StoriesViewModel
import com.ibnalqalibi.storyapp.utils.reduceFileImage
import com.ibnalqalibi.storyapp.utils.uriToFile
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var latitude: RequestBody? = null
    private var longitude: RequestBody? = null
    private var currentImageUri: Uri? = null
    private val viewModel by viewModels<StoriesViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.previewImageView.setImageURI(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }

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

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        with(binding){
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCameraX() }
            topAppBar.setNavigationOnClickListener {
                finish()
            }
            buttonAdd.setOnClickListener { uploadImage() }

            locationSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(
                            this@AddStoryActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            this@AddStoryActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@AddStoryActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                            100
                        )
                        return@setOnCheckedChangeListener
                    } else {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                latitude = it.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                longitude = it.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            }
                        }
                    }
                }else{
                    latitude = null
                    longitude = null
                }
            }

        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CameraActivity.CAMERAX_RESULT) {

            val cameraImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)
            val galleryImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_GALLERY)

            if (cameraImageUri != null) {
                currentImageUri = Uri.parse(cameraImageUri)
            } else if (galleryImageUri != null) {
                currentImageUri = Uri.parse(galleryImageUri)
            } else {
                // Handle the case where neither extra is present (optional)
                Log.w("Image Selection", "No image data found in intent extras")
            }
            if (currentImageUri != null) {
                val uri = currentImageUri.toString().toUri()
                binding.previewImageView.setImageURI(uri)
            } else {
                Log.d("Photo Picker", "No media selected")
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.edAddDescription.text.toString()
            showLoading(true)

            val requestDesc = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            lifecycleScope.launch {
                viewModel.addStory(multipartBody, requestDesc, latitude, longitude)
                viewModel.message.observe(this@AddStoryActivity) { showToast(it) }
                viewModel.isLoading.observe(this@AddStoryActivity) { showLoading(it) }
                viewModel.isSuccess.observe(this@AddStoryActivity) { isSuccess ->
                    if (isSuccess) {
                        val intent = Intent(this@AddStoryActivity, StoriesActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}