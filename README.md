# Example using Retrofit to call the BioPass ID API

For this example we used the [Enroll](https://docs.biopassid.com/#c5743ec5-c513-4f32-ab41-91854a85200c) from the [Multibiometrics plan](https://panel.biopassid.com/pricing?menu=multibiometrics) and [Retrofit](https://square.github.io/retrofit/) to make http requests.

First, add the Retrofit package. To install the `Retrofit` package, add it to the dependencies section of the `app/build.gradle` file.

```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.5.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.5.0'
}
```

Additionally, in your AndroidManifest.xml file, add the permissions.

```xml
<!-- Required to fetch data from the internet. -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Required to save images to gallery. -->
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

## EnrollPersonRequest

Create the EnrollPersonRequest data class:

```Kotlin
package com.example.fingerprintdemo

import com.google.gson.annotations.SerializedName

data class FingerPersonRequest(
    @SerializedName("Finger-1") val finger1: String? = null,
    @SerializedName("Finger-2") val finger2: String? = null,
    @SerializedName("Finger-3") val finger3: String? = null,
    @SerializedName("Finger-4") val finger4: String? = null,
    @SerializedName("Finger-5") val finger5: String? = null,
    @SerializedName("Finger-6") val finger6: String? = null,
    @SerializedName("Finger-7") val finger7: String? = null,
    @SerializedName("Finger-8") val finger8: String? = null,
    @SerializedName("Finger-9") val finger9: String? = null,
    @SerializedName("Finger-10") val finger10: String? = null,
)

data class PersonRequest(
    @SerializedName("CustomID") val customID: String,
    @SerializedName("Fingers") val fingers: List<FingerPersonRequest>
)

data class EnrollPersonRequest(
    @SerializedName("Person") val person: PersonRequest
)
```

## EnrollPersonResponse

Create the EnrollPersonResponse data class:

```Kotlin
package com.example.fingerprintdemo

import com.google.gson.annotations.SerializedName

data class PersonResponse(
    @SerializedName("ClientID") val clientID: String,
    @SerializedName("CustomID") val customID: String,
    @SerializedName("BioPassID") val bioPassID: String
)

data class EnrollPersonResponse(
    @SerializedName("Person") val person: PersonResponse,
    @SerializedName("Message") val message: String
)
```

## BioPassIDApi

**Here, you will need an API key to be able to make requests to the BioPass ID API. To get your API key contact us through our website [BioPass ID](https://www.biopassid.com/)**.

Create the BioPassIDApi interface to make requests to the BioPass ID API:

```Kotlin
package com.example.fingerprintdemo

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BioPassIDApi {
    @Headers("Content-Type: application/json", "Ocp-Apim-Subscription-Key: your-api-key")
    @POST("multibiometrics/enroll")
    fun enrollPerson(@Body enrollPersonRequest: EnrollPersonRequest) : Call<EnrollPersonResponse>
}
```

## Network

Create the Network class to make requests to the BioPass ID API:

```Kotlin
package com.example.fingerprintdemo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Network {
    companion object {

        /** Returns a Client Retrofit Instance for Requests
         */
        fun getRetrofitInstance() : BioPassIDApi {
            return Retrofit.Builder()
                .baseUrl("https://api.biopassid.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BioPassIDApi::class.java)
        }
    }
}
```

## MediaStoreUtils

Create the MediaStoreUtils class to save images to gallery:

```Kotlin
package com.example.fingerprintdemo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object MediaStoreUtils {
    fun saveImage(context: Context, bitmap: Bitmap) {
        val folderName = "Fingerprint"
        val date = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val fileName = "fingerprint_${date}.png"
        val values = contentValues(bitmap.width, bitmap.height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val directory = File("${Environment.getExternalStorageDirectory()}/Pictures/$folderName")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            try {
                saveImageToStream(bitmap, FileOutputStream(file))
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun contentValues(width: Int, height: Int) : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.WIDTH, width)
        values.put(MediaStore.Images.Media.HEIGHT, height)
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
```

## In xml layout of your main activity

```XML
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btnCaptureRightHand"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="15dp"
        android:text="Capture Right Hand" />

    <Button
        android:id="@+id/btnCaptureLeftHand"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="15dp"
        android:text="Capture Left Hand" />

    <Button
        android:id="@+id/btnCaptureThumbs"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="15dp"
        android:text="Capture Thumbs" />

</LinearLayout>
```

## In your main activity

```Kotlin
package com.example.fingerprintdemo

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

    private val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    private val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    private var readPermissionGranted = false
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
                readPermissionGranted = permissions[readPermission] ?: readPermissionGranted
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
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            readPermission
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            writePermission
        ) == PackageManager.PERMISSION_GRANTED
        val isMinSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || isMinSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!readPermissionGranted) {
            permissionsToRequest.add(readPermission)
        }
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
```