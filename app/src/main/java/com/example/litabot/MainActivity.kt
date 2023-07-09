package com.example.litabot

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.Camera
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null
    private lateinit var captureButton: Button
    private lateinit var anotherButton: Button

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        // Set the display orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        captureButton = findViewById<Button>(R.id.btnCapture)
        anotherButton = findViewById<Button>(R.id.anotherButton)
        captureButton.setOnClickListener {
            takePicture()
            captureButton.visibility = View.GONE
            anotherButton.visibility = View.VISIBLE
        }
        anotherButton.setOnClickListener {
            startCameraPreview()
            anotherButton.visibility = View.GONE
            captureButton.visibility = View.VISIBLE
        }
        surfaceView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val focusRect = calculateFocusArea(event.x, event.y) // Calculate the focus area based on the touch coordinates
                performAutoFocus(focusRect) // Trigger autofocus using the focus area
            }
            true
        }
    }

    private fun calculateFocusArea(x: Float, y: Float): Rect {
        val previewSize = camera?.parameters?.previewSize
        previewSize?.let {
            val focusRectSize = 200 // Size of the focus area rectangle in pixels (adjust as needed)

            val centerX = x / it.width
            val centerY = y /it.height

            val left = clamp(centerX - 0.5f, -0.5f, 0.5f) * 2000 + 1000
            val top = clamp(centerY - 0.5f, -0.5f, 0.5f) * 2000 + 1000
            val right = left + focusRectSize
            val bottom = top + focusRectSize

            return Rect(
                (left - 1000).toInt(),
                (top - 1000).toInt(),
                (right - 1000).toInt(),
                (bottom - 1000).toInt()
            )
        }
        return Rect(-1000, -1000, 1000, 1000)
    }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return Math.max(min, Math.min(max, value))
    }

    private fun performAutoFocus(focusRect: Rect) {
        val parameters = camera?.parameters
        parameters?.let {
            if (it.focusMode == Camera.Parameters.FOCUS_MODE_AUTO ||
                it.focusMode == Camera.Parameters.FOCUS_MODE_MACRO
            ) {
                it.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                if (it.maxNumFocusAreas > 0) {
                    val focusAreas = mutableListOf<Camera.Area>()
                    focusAreas.add(Camera.Area(focusRect, 1000))
                    it.focusAreas = focusAreas
                }
                camera?.cancelAutoFocus()
                camera?.autoFocus { success, _ ->
                    if (success) {
                        // Focus operation succeeded
                    } else {
                        // Focus operation failed
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview()
        } else {
            requestCameraPermission()
        }
    }

    private fun takePicture() {
        camera?.takePicture(null, null, PictureCallback())
    }

    private inner class PictureCallback : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
            // Process the captured image data here
        }
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, acquire the camera and start the preview
        startCameraPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface changed, reset the camera preview
        stopCameraPreview()
        startCameraPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface destroyed, release the camera
        stopCameraPreview()
    }

    private fun startCameraPreview() {
        try {
            camera = Camera.open()
            val parameters = camera?.parameters
            parameters?.let {
                it.setRotation(90)
                camera?.setDisplayOrientation(90)
                camera?.parameters = it
                val previewSize = it.previewSize
                surfaceView.layoutParams.height = previewSize.width
            }
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopCameraPreview() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraPreview()
            } else {
                // Camera permission denied, handle accordingly (e.g., show a message or exit the app)
            }
        }
    }
}