package com.example.lens_bridge

import android.content.Context
import android.graphics.Color
import android.view.View
import io.flutter.plugin.platform.PlatformView

class CameraPlatformView(
    context: Context,
    viewId: Int

) : PlatformView {

    private val nativeView: View = View(context).apply {
        setBackgroundColor(Color.rgb(30, 200, 120))
    }

    override fun getView(): View = nativeView

    override fun dispose() {

    }
}