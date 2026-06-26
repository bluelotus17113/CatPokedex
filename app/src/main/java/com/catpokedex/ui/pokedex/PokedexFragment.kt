package com.catpokedex.ui.pokedex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.catpokedex.CatPokedexApp
import com.catpokedex.R
import com.catpokedex.data.Cat
import com.catpokedex.data.CatViewModel
import com.catpokedex.data.ZenSoundManager
import com.catpokedex.databinding.FragmentPokedexBinding

class PokedexFragment : Fragment() {
    private var _binding: FragmentPokedexBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CatViewModel by viewModels()
    private lateinit var adapter: CatAdapter
    private lateinit var soundManager: ZenSoundManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPokedexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundManager = (requireActivity().application as CatPokedexApp).soundManager

        adapter = CatAdapter { cat ->
            soundManager.play(ZenSoundManager.SOUND_OPEN)
            val bundle = Bundle().apply {
                putInt("catId", cat.id)
            }
            findNavController().navigate(R.id.action_pokedex_to_detail, bundle)
        }

        binding.recyclerCats.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerCats.adapter = adapter

        val count = viewModel.catCount.value ?: 0
        updateCount(count)

        viewModel.cats.observe(viewLifecycleOwner) { cats ->
            adapter.submitList(cats)
            binding.emptyState.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerCats.visibility = if (cats.isEmpty()) View.GONE else View.VISIBLE
            updateCount(cats.size)
        }

        // Animate entrance
        binding.recyclerCats.post {
            binding.recyclerCats.alpha = 0f
            binding.recyclerCats.animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    private fun updateCount(count: Int) {
        binding.tvCatCount.text = getString(R.string.total_cats, count)
    }

    override fun onResume() {
        super.onResume()
        soundManager.play(ZenSoundManager.SOUND_NAVIGATE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
