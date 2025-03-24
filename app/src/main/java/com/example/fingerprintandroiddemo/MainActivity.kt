package com.example.fingerprintandroiddemo

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.biopassid.fingerprintsdk.Fingerprint
import br.com.biopassid.fingerprintsdk.config.FingerprintConfig
import br.com.biopassid.fingerprintsdk.config.enums.FingerprintCaptureType
import br.com.biopassid.fingerprintsdk.engine.FingerprintCaptureListener
import br.com.biopassid.fingerprintsdk.engine.FingerprintCaptureState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var btnCaptureRightHand: Button
    private lateinit var btnCaptureLeftHand: Button
    private lateinit var btnCaptureThumbs: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                writePermissionGranted = permissions[writePermission] ?: writePermissionGranted
            }
        updateOrRequestPermissions()

        // Button in your xml layout responsible for calling the Fingerprint SDK
        btnCaptureRightHand = findViewById(R.id.btnCaptureRightHand)
        btnCaptureLeftHand = findViewById(R.id.btnCaptureLeftHand)
        btnCaptureThumbs = findViewById(R.id.btnCaptureThumbs)

        // Instantiates Fingerprint config by passing your license key
        val config = FingerprintConfig(licenseKey = "your-license-key")

        // Initializes fingerprint capture
        btnCaptureRightHand.setOnClickListener {
            config.captureType = FingerprintCaptureType.RIGHT_HAND_FINGERS
            Fingerprint.takeFingerprint(this, config, object : FingerprintCaptureListener {
                override fun onFingerCapture(images: List<Bitmap>, error: String?) {
                    if (images.isNotEmpty()) {
                        // Save images to gallery
                        images.forEach { image ->
                            MediaStoreUtils.saveImage(this@MainActivity, image)
                        }

                        // Encode Bitmap to base64 string
                        val rightLittle = bitmapToBas64(images[1])
                        val rightRing = bitmapToBas64(images[2])
                        val rightMiddle = bitmapToBas64(images[3])
                        val rightIndex = bitmapToBas64(images[4])

                        // Send images to API
                        sendImages(
                            rightIndex = rightIndex,
                            rightMiddle = rightMiddle,
                            rightRing = rightRing,
                            rightLittle = rightLittle
                        )
                    }
                }

                override fun onFingerDetected(fingerRects: List<Rect>) {}

                override fun onStatusChanged(state: FingerprintCaptureState) {}
            })
        }

        btnCaptureLeftHand.setOnClickListener {
            config.captureType = FingerprintCaptureType.LEFT_HAND_FINGERS
            Fingerprint.takeFingerprint(this, config, object : FingerprintCaptureListener {
                override fun onFingerCapture(images: List<Bitmap>, error: String?) {
                    if (images.isNotEmpty()) {
                        // Save images to gallery
                        images.forEach { image ->
                            MediaStoreUtils.saveImage(this@MainActivity, image)
                        }

                        // Encode Bitmap to base64 string
                        val leftLittle = bitmapToBas64(images[1])
                        val leftRing = bitmapToBas64(images[2])
                        val leftMiddle = bitmapToBas64(images[3])
                        val leftIndex = bitmapToBas64(images[4])

                        // Send images to API
                        sendImages(
                            leftIndex = leftIndex,
                            leftMiddle = leftMiddle,
                            leftRing = leftRing,
                            leftLittle = leftLittle
                        )
                    }
                }

                override fun onFingerDetected(fingerRects: List<Rect>) {}

                override fun onStatusChanged(state: FingerprintCaptureState) {}
            })
        }

        btnCaptureThumbs.setOnClickListener {
            config.captureType = FingerprintCaptureType.THUMBS
            Fingerprint.takeFingerprint(this, config, object : FingerprintCaptureListener {
                override fun onFingerCapture(images: List<Bitmap>, error: String?) {
                    if (images.isNotEmpty()) {
                        // Save images to gallery
                        images.forEach { image ->
                            MediaStoreUtils.saveImage(this@MainActivity, image)
                        }

                        // Encode Bitmap to base64 string
                        val leftThumb = bitmapToBas64(images[1])
                        val rightThumb = bitmapToBas64(images[2])

                        // Send images to API
                        sendImages(rightThumb = rightThumb, leftThumb = leftThumb)
                    }
                }

                override fun onFingerDetected(fingerRects: List<Rect>) {}

                override fun onStatusChanged(state: FingerprintCaptureState) {}
            })
        }
    }

    // Helper method used to send images to API
    private fun sendImages(
        rightThumb: String? = null,
        rightIndex: String? = null,
        rightMiddle: String? = null,
        rightRing: String? = null,
        rightLittle: String? = null,
        leftThumb: String? = null,
        leftIndex: String? = null,
        leftMiddle: String? = null,
        leftRing: String? = null,
        leftLittle: String? = null
    ) {
        // Instantiate Enroll request
        val enrollPersonRequest = EnrollPersonRequest(
            PersonRequest(
                "your-customID",
                listOf(
                    FingerPersonRequest(
                        finger1 = rightThumb,
                        finger2 = rightIndex,
                        finger3 = rightMiddle,
                        finger4 = rightRing,
                        finger5 = rightLittle,
                        finger6 = leftThumb,
                        finger7 = leftIndex,
                        finger8 = leftMiddle,
                        finger9 = leftRing,
                        finger10 = leftLittle
                    )
                )
            )
        )

        // Get retrofit
        val retrofit = Network.getRetrofitInstance()

        // Execute request to the BioPass ID API
        val callback: Call<EnrollPersonResponse> =
            retrofit.enrollPerson(enrollPersonRequest)

        // Handle API response
        callback.enqueue(object : Callback<EnrollPersonResponse?> {
            override fun onResponse(
                call: Call<EnrollPersonResponse?>,
                response: Response<EnrollPersonResponse?>
            ) {
                Log.d(TAG, "code: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d(TAG, "body: ${response.body()}")
                } else {
                    Log.d(TAG, "message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<EnrollPersonResponse?>, t: Throwable) {
                Log.e(TAG, "Error trying to call enroll person. ", t)
            }
        })
    }

    // Helper method used to convert Bitmap to Base64
    private fun bitmapToBas64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        try {
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // Helper method used to request permissions
    private fun updateOrRequestPermissions() {
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            writePermission
        ) == PackageManager.PERMISSION_GRANTED
        val isMinSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        writePermissionGranted = hasWritePermission || isMinSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(writePermission)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    companion object {
        private const val TAG = "FingerprintDemo"
    }
}