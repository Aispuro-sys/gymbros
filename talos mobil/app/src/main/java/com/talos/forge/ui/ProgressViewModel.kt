package com.talos.forge.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.models.ProgressPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProgressViewModel(private val repository: Repository) : ViewModel() {

    private val _photos = MutableStateFlow<List<ProgressPhoto>>(emptyList())
    val photos: StateFlow<List<ProgressPhoto>> = _photos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedPhotoIndex = MutableStateFlow(-1)
    val selectedPhotoIndex: StateFlow<Int> = _selectedPhotoIndex.asStateFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _photos.value = repository.getProgressPhotos()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPhoto(uri: Uri, context: android.content.Context, weight: Float? = null) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    _error.value = "No se pudo cargar la imagen"
                    return@launch
                }

                val maxDim = 1024
                val ratio = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                val scaledBitmap = if (ratio < 1f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * ratio).toInt(),
                        (bitmap.height * ratio).toInt(),
                        true
                    )
                } else {
                    bitmap
                }

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                val dataUrl = "data:image/jpeg;base64,$base64"

                repository.uploadProgressPhoto(dataUrl, weight)
                loadPhotos()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteProgressPhoto(id)
                _photos.value = _photos.value.filter { it.id != id }
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun updateWeight(id: String, weight: Float?) {
        viewModelScope.launch {
            try {
                val updated = repository.updateProgressPhoto(id, weight)
                _photos.value = _photos.value.map { if (it.id == id) updated else it }
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun selectPhoto(index: Int) {
        _selectedPhotoIndex.value = index
    }

    fun clearSelectedPhoto() {
        _selectedPhotoIndex.value = -1
    }

    fun clearError() {
        _error.value = null
    }
}
