package com.catpokedex.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.catpokedex.CatPokedexApp
import com.catpokedex.R
import com.catpokedex.data.CatDetectorHelper
import com.catpokedex.data.CatViewModel
import com.catpokedex.data.ZenSoundManager
import com.catpokedex.databinding.FragmentCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CatViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var isFrontCamera = false
    private lateinit var soundManager: ZenSoundManager
    private lateinit var catDetector: CatDetectorHelper
    private lateinit var analysisExecutor: ExecutorService
    private var isAnalyzing = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Se necesita permiso de cámara", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundManager = (requireActivity().application as CatPokedexApp).soundManager
        catDetector = CatDetectorHelper()
        analysisExecutor = Executors.newSingleThreadExecutor()

        binding.toolbar.setNavigationOnClickListener {
            soundManager.play(ZenSoundManager.SOUND_NAVIGATE)
            findNavController().navigateUp()
        }

        binding.captureBtn.setOnClickListener {
            if (isAnalyzing) return@setOnClickListener

            it.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
                .start()
            takePhotoAndAnalyze()
        }

        binding.switchCameraBtn.setOnClickListener {
            soundManager.play(ZenSoundManager.SOUND_NAVIGATE)
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imgCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageCapture = imgCapture

            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imgCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                Toast.makeText(requireContext(), "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Toma una foto y la analiza para detectar si hay un gato.
     * Solo guarda la foto si se detecta un gato.
     */
    private fun takePhotoAndAnalyze() {
        val imgCapture = imageCapture ?: return

        isAnalyzing = true
        showAnalyzingState(true)

        // Capturamos el frame como ImageProxy para analizar
        imgCapture.takePicture(
            analysisExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = catDetector.imageProxyToBitmap(image)
                    image.close()

                    // Analizar en background
                    catDetector.detectCat(bitmap) { isCat, confidence ->
                        activity?.runOnUiThread {
                            if (isCat) {
                                // ¡Es un gato! Guardar la foto
                                savePhoto(confidence)
                            } else {
                                // No es un gato
                                showNotACatFeedback()
                                showAnalyzingState(false)
                                isAnalyzing = false
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Error al capturar foto", Toast.LENGTH_SHORT).show()
                        showAnalyzingState(false)
                        isAnalyzing = false
                    }
                }
            }
        )
    }

    /**
     * Guarda la foto final después de confirmar que es un gato.
     */
    private fun savePhoto(confidence: Float) {
        val imgCapture = imageCapture ?: return

        soundManager.play(ZenSoundManager.SOUND_SHUTTER)

        val photoFile = File(
            requireContext().filesDir,
            "cat_photos/cat_${System.currentTimeMillis()}.jpg"
        ).apply { parentFile?.mkdirs() }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imgCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    soundManager.play(ZenSoundManager.SOUND_CAPTURE)
                    showAnalyzingState(false)
                    isAnalyzing = false

                    val bundle = Bundle().apply {
                        putString("photoPath", photoFile.absolutePath)
                    }
                    findNavController().navigate(R.id.action_camera_to_capture, bundle)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed", exception)
                    Toast.makeText(requireContext(), "Error al guardar foto", Toast.LENGTH_SHORT).show()
                    showAnalyzingState(false)
                    isAnalyzing = false
                }
            }
        )
    }

    /**
     * Muestra feedback visual cuando no se detectó un gato.
     */
    private fun showNotACatFeedback() {
        soundManager.play(ZenSoundManager.SOUND_ERROR)
        Toast.makeText(
            requireContext(),
            getString(R.string.not_a_cat),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Muestra/oculta el indicador de "analizando..."
     */
    private fun showAnalyzingState(analyzing: Boolean) {
        // Podríamos mostrar un overlay de "analizando" acá
        binding.captureBtn.isEnabled = !analyzing
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        analysisExecutor.shutdown()
        catDetector.close()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}
