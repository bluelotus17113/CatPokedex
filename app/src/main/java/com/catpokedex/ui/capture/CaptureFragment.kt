package com.catpokedex.ui.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.catpokedex.CatPokedexApp
import com.catpokedex.R
import com.catpokedex.data.Cat
import com.catpokedex.data.CatType
import com.catpokedex.data.CatViewModel
import com.catpokedex.data.ZenSoundManager
import com.catpokedex.databinding.FragmentCaptureBinding
import java.io.File

class CaptureFragment : Fragment() {
    private var _binding: FragmentCaptureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CatViewModel by viewModels()
    private lateinit var photoPath: String
    private lateinit var soundManager: ZenSoundManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundManager = (requireActivity().application as CatPokedexApp).soundManager
        photoPath = arguments?.getString("photoPath") ?: ""

        binding.toolbar.setNavigationOnClickListener {
            soundManager.play(ZenSoundManager.SOUND_NAVIGATE)
            findNavController().navigateUp()
        }

        // Load preview with soft fade
        val photoFile = File(photoPath)
        if (photoFile.exists()) {
            binding.ivPreview.load(photoFile) {
                crossfade(500)
            }
        }

        // Animate form entrance
        binding.formCard.alpha = 0f
        binding.formCard.translationY = 80f
        binding.formCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Setup type dropdown
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            CatType.allDisplayNames
        )
        binding.actvType.setAdapter(adapter)
        binding.actvType.setText(CatType.NORMAL.displayName, false)

        // Save button
        binding.saveBtn.setOnClickListener {
            val name = binding.etCatName.text.toString().trim()
            val type = binding.actvType.text.toString()

            if (name.isEmpty()) {
                binding.etCatName.error = "Dale un nombre al gato"
                return@setOnClickListener
            }
            if (type.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona un tipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Success animation
            binding.saveBtn.animate()
                .scaleX(0.95f).scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    binding.saveBtn.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
                .start()

            val cat = Cat(
                name = name,
                type = type,
                photoPath = photoPath
            )
            viewModel.insertCat(cat)

            // Play save sound
            soundManager.play(ZenSoundManager.SOUND_SAVE)

            Toast.makeText(
                requireContext(),
                getString(R.string.cat_saved, name),
                Toast.LENGTH_SHORT
            ).show()

            // Navigate back to pokedex
            findNavController().popBackStack(R.id.pokedexFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
