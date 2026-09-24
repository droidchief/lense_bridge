package com.example.lens_bridge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.google.mlkit.vision.face.Face

class OverlayView(context: Context) : View(context) {

    private var faces: List<Face> = emptyList()
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    fun updateFaces(newFaces: List<Face>, imgWidth: Int, imgHeight: Int) {
        faces = newFaces
        imageWidth = imgWidth
        imageHeight = imgHeight
        postInvalidate() // triggers onDraw from any thread safely
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaleX = width.toFloat() / imageHeight  // note: swapped, see decisions.md
        val scaleY = height.toFloat() / imageWidth

        for (face in faces) {
            val box = face.boundingBox
            canvas.drawRect(
                box.left * scaleX,
                box.top * scaleY,
                box.right * scaleX,
                box.bottom * scaleY,
                boxPaint
            )
        }
    }
}