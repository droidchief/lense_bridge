package com.example.lens_bridge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import io.flutter.plugin.platform.PlatformView

class CameraPlatformView(
    private val context: Context,
    viewId: Int,
    private val lifecycleOwner: LifecycleOwner
) : PlatformView {

    private val previewView: PreviewView = PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
    private val overlayView: OverlayView = OverlayView(context)
    private val container: FrameLayout = FrameLayout(context).apply {
        addView(
            previewView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        addView(
            overlayView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var currentSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    init {
        startCamera()
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindPreview()
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        overlayView.visibility = View.INVISIBLE
        previewView.visibility = View.INVISIBLE
    }

    @OptIn(ExperimentalGetImage::class)
    fun switchLens() {
        currentSelector = if (currentSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindPreview()
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun bindPreview() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                overlayView.updateFaces(faces, inputImage.width, inputImage.height)
                            }
                            .addOnCompleteListener {
                                imageProxy.close() // MUST close every frame, or CameraX stalls
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, currentSelector, preview, analysis)
        overlayView.visibility = View.VISIBLE
        previewView.visibility = View.VISIBLE
    }

    override fun getView(): View = container

    override fun dispose() {
        cameraProvider?.unbindAll()
        faceDetector.close()
    }
}