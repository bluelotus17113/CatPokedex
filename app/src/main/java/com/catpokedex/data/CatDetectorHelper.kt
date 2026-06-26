package com.catpokedex.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.ByteArrayOutputStream

/**
 * Helper para detectar gatos en imágenes usando ML Kit Object Detection (on-device, offline).
 * El modelo base de ML Kit puede detectar "Cat" entre sus categorías.
 */
class CatDetectorHelper {

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification() // Necesitamos clasificación para obtener la categoría "Cat"
            .build()
    )

    /**
     * Detecta si hay un gato en la imagen.
     * @param bitmap La imagen a analizar
     * @param callback Devuelve true si detectó un gato con confianza aceptable
     */
    fun detectCat(bitmap: Bitmap, callback: (Boolean, Float) -> Unit) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        objectDetector.process(inputImage)
            .addOnSuccessListener { objects ->
                // Buscar entre los objetos detectados si hay un "Cat"
                val catObject = objects.firstOrNull { detectedObject ->
                    detectedObject.labels.any { label ->
                        label.text.equals("Cat", ignoreCase = true) && label.confidence > CONFIDENCE_THRESHOLD
                    }
                }

                if (catObject != null) {
                    val confidence = catObject.labels.first { it.text.equals("Cat", ignoreCase = true) }.confidence
                    callback(true, confidence)
                } else {
                    callback(false, 0f)
                }
            }
            .addOnFailureListener {
                // Si falla la detección, permitir la foto (fail-open para no bloquear al usuario)
                callback(true, 0f)
            }
    }

    /**
     * Convierte un ImageProxy (de CameraX) a Bitmap para análisis.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val planes = imageProxy.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun close() {
        objectDetector.close()
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.5f // 50% confianza mínima
    }
}
