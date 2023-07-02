package com.example.litabot

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var camera: Camera? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        // Set the display orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview()
        } else {
            requestCameraPermission()
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
            parameters?.setRotation(90)
            camera?.setDisplayOrientation(90)
            camera?.parameters = parameters
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