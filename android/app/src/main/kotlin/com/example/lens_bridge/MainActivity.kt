package com.example.lens_bridge

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.platform.PlatformViewRegistry

class MainActivity : FlutterActivity() {

    private val cameraPermissionCode = 100


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

        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "lensbridge/camera_preview", // must match the Dart viewType exactly
                CameraPlatformViewFactory(this)
            )
    }
}