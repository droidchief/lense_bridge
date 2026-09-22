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

    init {
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        }, ContextCompat.getMainExecutor(context))
    }

    override fun getView() = previewView

    override fun dispose() {

    }
}