package com.example.lens_bridge

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.platform.PlatformView

class CameraPlatformView(
    private val context: Context,
    viewId: Int,
    private val lifecycleOwner: LifecycleOwner
) : PlatformView {

    private val previewView: PreviewView = PreviewView(context)
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


    init {
        startCamera()
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindPreview()
            previewView.visibility = android.view.View.VISIBLE
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        previewView.visibility = android.view.View.INVISIBLE
    }

    fun switchLens() {
        currentSelector = if (currentSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindPreview()
    }

    private fun bindPreview() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, currentSelector, preview)
    }

    override fun getView() = previewView

    override fun dispose() {
        cameraProvider?.unbindAll()
    }
}