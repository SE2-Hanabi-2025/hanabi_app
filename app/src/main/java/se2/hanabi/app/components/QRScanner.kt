package se2.hanabi.app.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScanner(
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val TAG = "QRScanner"
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                Log.d(TAG, "Creating QR Scanner view")
                val previewView = PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        Log.d(TAG, "Initializing camera")
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        val barcodeScanner = BarcodeScanning.getClient()
                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        Log.d(TAG, "Processing image for barcode detection")
                                        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                        barcodeScanner.process(inputImage)
                                            .addOnSuccessListener { barcodes ->
                                                Log.d(TAG, "Barcode scan successful, found ${barcodes.size} barcodes")
                                                if (barcodes.isNotEmpty()) {
                                                    val barcode = barcodes.first()
                                                    val rawValue = barcode.rawValue
                                                    Log.d(TAG, "Barcode value: $rawValue")
                                                    if (rawValue != null && rawValue.length == 6) {
                                                        Log.d(TAG, "Valid barcode found: $rawValue")
                                                        // QR-Code erkannt, Scanner deaktivieren und Callback ausführen
                                                        cameraProvider.unbindAll()
                                                        onScanned(rawValue.uppercase())
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Barcode scanning failed: ${e.message}")
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        Log.d(TAG, "No image available from imageProxy")
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            Log.d(TAG, "Binding camera use cases")
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                context as LifecycleOwner, cameraSelector, preview, analyzer
                            )
                            Log.d(TAG, "Camera successfully bound to lifecycle")
                        } catch (exc: Exception) {
                            Log.e(TAG, "Camera binding failed: ${exc.message}", exc)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera provider future failed: ${e.message}", e)                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {

        }
        
        // Close button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), 
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = { 
                    Log.d(TAG, "QR Scanner closed by user")
                    onDismiss() 
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Close Scanner",
                    tint = Color.White
                )
            }
        }
    }
}
