package com.example.lens_bridge

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class CameraPlatformViewFactory (
    private val lifecycleOwner: LifecycleOwner

) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return CameraPlatformView(context, viewId, lifecycleOwner)
    }
}