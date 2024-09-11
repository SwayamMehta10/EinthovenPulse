package com.example.einthovenpulse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

class MeasureHeartRate(private val context: Context) {
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var cameraControl: CameraControl? = null // Store camera control reference
    private var heartRateCallback: ((Int) -> Unit)? = null // Callback for heart rate

    fun setHeartRateCallback(callback: (Int) -> Unit) {
        heartRateCallback = callback
    }

    fun startCameraPreview(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    (context as ComponentActivity),
                    cameraSelector,
                    preview
                )

                // Store camera control reference
                cameraControl = camera.cameraControl

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(context: Context, previewView: PreviewView, isHeartRateMeasured: MutableState<Boolean>) {
        // Video Capture
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST)) // Optional: Set the desired video quality
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        // Bind camera lifecycle
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            // Use case to record the video
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    (context as ComponentActivity), cameraSelector, preview, videoCapture
                )

                // Turn on flash
                cameraControl?.enableTorch(true)

                // Start recording
                val videoFile = File(
                    context.getExternalFilesDir(null),
                    "heart_rate_measurement.mp4"
                )

                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                val recording = videoCapture.output.prepareRecording(context, outputOptions)
                    .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                        when (recordEvent) {
                            is VideoRecordEvent.Start -> {
                                Log.d("VideoRecording", "Recording Started")
                            }

                            is VideoRecordEvent.Finalize -> {
                                if (!recordEvent.hasError()) {
                                    val uri = recordEvent.outputResults.outputUri
                                    // Process the video for heart rate calculation
                                    processVideoForHeartRate(uri, isHeartRateMeasured)
                                } else {
                                    // Handle error
                                }

                                // Turn off flash
                                cameraControl?.enableTorch(false)

                                // Close camera preview
                                cameraProvider.unbindAll()  // Unbind all use cases to close the camera preview

                            }
                        }
                    }

                // Stop recording after 45 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    recording.stop()
                    Log.d("Recording", "Recording complete")
                }, 45000)

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun processVideoForHeartRate(uri: Uri, isHeartRateMeasured: MutableState<Boolean>) {
        // Call the heartRateCalculator function with the video Uri
        (context as ComponentActivity).lifecycleScope.launch {
            Log.d("URI", "URI : $uri")
            Log.d("Context", "Context: $context")
            val heartRate = heartRateCalculator(uri)
            Log.d("HeartRate", "Calculated Heart Rate: $heartRate")
            heartRateCallback?.invoke(heartRate) // Use callback to update heart rate
            isHeartRateMeasured.value = true
        }
    }

    private suspend fun heartRateCalculator(uri: Uri): Int {
        return withContext(Dispatchers.IO) {
            Log.d("URI check", "Uri: $uri")
            Log.d("Context check", "Context: $context")
            val result: Int
            /*
            val proj = arrayOf(MediaStore.Video.Media.DATA)
            val cursor = contentResolver.query(uri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor?.moveToFirst()
            val path = cursor?.getString(columnIndex ?: 0)
            cursor?.close()
            */

            val retriever = MediaMetadataRetriever()
            val frameList = ArrayList<Bitmap>()
            try {
                retriever.setDataSource(context, uri)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                val frameDuration = min(duration?.toIntOrNull() ?: 0, 425)
                var i = 10
                while (i < frameDuration) {
                    val bitmap = retriever.getFrameAtIndex(i)
                    bitmap?.let { frameList.add(it) }
                    i += 15
                }
            } catch (e: Exception) {
                Log.d("MediaPath", "convertMediaUriToPath: ${e.stackTrace} ")
            } finally {
                retriever.release()
                var redBucket: Long
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (i in frameList) {
                    redBucket = 0
                    for (y in 350 until 450) {
                        for (x in 350 until 450) {
                            val c: Int = i.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) +
                                    Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }
                Log.d("Check a", "Size of a: ${a.size}")
                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    val temp =
                        (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
                                + a.elementAt(
                            i + 3
                        ) + a.elementAt(
                            i + 4
                        )) / 4
                    b.add(temp)
                }
                Log.d("Check b", "Size of b: ${b.size}")
                var x = b.elementAt(0)
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b.elementAt(i)
                    if ((p - x) > 3500) {
                        count += 1
                    }
                    x = b.elementAt(i)
                }
                val rate = ((count.toFloat()) * 60).toInt()
                result = (rate / 4)
            }
            result
        }
    }
}