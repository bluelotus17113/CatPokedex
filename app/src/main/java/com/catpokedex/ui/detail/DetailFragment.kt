package com.catpokedex.ui.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.catpokedex.databinding.FragmentDetailBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CatViewModel by viewModels()
    private var currentCat: Cat? = null
    private lateinit var soundManager: ZenSoundManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundManager = (requireActivity().application as CatPokedexApp).soundManager

        binding.toolbar.setNavigationOnClickListener {
            soundManager.play(ZenSoundManager.SOUND_NAVIGATE)
            findNavController().navigateUp()
        }

        binding.deleteBtn.setOnClickListener {
            currentCat?.let { cat ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Liberar gato")
                    .setMessage(getString(R.string.confirm_delete, cat.name))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.deleteCat(cat)
                        try { File(cat.photoPath).delete() } catch (_: Exception) {}
                        soundManager.play(ZenSoundManager.SOUND_DELETE)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.cat_deleted, cat.name),
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        }

        val catId = arguments?.getInt("catId") ?: -1
        if (catId >= 0) {
            viewModel.getCatById(catId) { cat ->
                cat?.let { bindCat(it) }
            }
        }
    }

    private fun bindCat(cat: Cat) {
        currentCat = cat
        val type = CatType.fromDisplayName(cat.type)

        val context = binding.root.context

        binding.toolbar.title = cat.name
        binding.tvCatName.text = cat.name
        binding.tvCatType.text = type.displayName
        binding.tvCatNumber.text = String.format("#%03d", cat.id)

        // Soft type indicator
        val typeColor = type.colorHex.toInt()
        binding.typeIndicator.setBackgroundColor(Color.argb(102, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor)))

        // Type chip
        val chipBg = binding.tvCatType.background as? GradientDrawable
        chipBg?.setColor(Color.argb(21, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor)))

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        binding.tvCapturedDate.text = getString(R.string.captured_on, sdf.format(Date(cat.capturedAt)))

        val photoFile = File(cat.photoPath)
        if (photoFile.exists()) {
            binding.ivCatPhoto.load(photoFile) { crossfade(600) }
        }

        // Animate entrance
        binding.ivCatPhoto.alpha = 0f
        binding.ivCatPhoto.animate()
            .alpha(1f)
            .setDuration(700)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        binding.infoCard.alpha = 0f
        binding.infoCard.translationY = 60f
        binding.infoCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
