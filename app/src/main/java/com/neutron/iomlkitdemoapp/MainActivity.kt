package com.neutron.iomlkitdemoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 111
    private var imageCapture: ImageCapture? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!allPermissionsGranted()){
            requestPermission()
        }

        startCamera()

        val captureButton: FloatingActionButton = findViewById(R.id.image_capture_button)

        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()


            val previewView: PreviewView = findViewById(R.id.viewFinder)
            // Preview
            val preview = Preview.Builder()
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)


            //Initialize camera
            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch(exc: Exception) {
                Log.e("Neutron", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture: ImageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    recognizeText(image)
                    Toast.makeText(this@MainActivity,"Captured successfully .....", Toast.LENGTH_LONG).show()
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {

                    super.onError(exception)
                }
            }
        )

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun recognizeText(imageProxy: ImageProxy) {
        val image: InputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val detectedText: String = result.text

                //pass the text to new activity
                val intent = Intent(this, ScannedDetailsActivity::class.java)
                intent.putExtra("TEXT",detectedText)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
               Toast.makeText(this, "Couldn't Scan \n${e.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
                startCamera()
            }
        }
    }
}