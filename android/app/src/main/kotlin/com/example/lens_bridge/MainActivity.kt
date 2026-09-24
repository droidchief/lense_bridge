package com.example.lens_bridge

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val cameraPermissionCode = 100
    private lateinit var cameraPlatformViewFactory: CameraPlatformViewFactory

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )
        }

        cameraPlatformViewFactory = CameraPlatformViewFactory(this)

        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "lensbridge/camera_preview",
                cameraPlatformViewFactory
            )

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "lensbridge/camera_controller"
        ).setMethodCallHandler { call, result ->
            val activeView = cameraPlatformViewFactory.activeView

            if (activeView == null) {
                result.error("NO_VIEW", "Camera view not yet created", null)
                return@setMethodCallHandler
            }

            when (call.method) {
                "switchLens" -> {
                    activeView.switchLens()
                    result.success(null)
                }
                "stopCamera" -> {
                    activeView.stopCamera()
                    result.success(null)
                }
                "startCamera" -> {
                    activeView.startCamera()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }
}