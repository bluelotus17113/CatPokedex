package com.catpokedex.ui.pokedex

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.catpokedex.data.Cat
import com.catpokedex.data.CatType
import com.catpokedex.databinding.ItemCatBinding
import java.io.File

class CatAdapter(private val onClick: (Cat) -> Unit) :
    ListAdapter<Cat, CatAdapter.CatViewHolder>(CatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val binding = ItemCatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class CatViewHolder(private val binding: ItemCatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cat: Cat, position: Int) {
            val type = CatType.fromDisplayName(cat.type)

            binding.tvCatName.text = cat.name
            binding.tvCatType.text = type.displayName
            binding.tvCatNumber.text = String.format("#%03d", cat.id)

            val typeColor = type.colorHex.toInt()
            binding.typeIndicator.setBackgroundColor(
                Color.argb(136, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor))
            )

            val chipBg = binding.tvCatType.background as? GradientDrawable
            chipBg?.setColor(
                Color.argb(21, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor))
            )

            val photoFile = File(cat.photoPath)
            if (photoFile.exists()) {
                binding.ivCatPhoto.load(photoFile) {
                    crossfade(400)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }
            }

            binding.root.setOnClickListener { onClick(cat) }

            binding.root.alpha = 0f
            binding.root.translationY = 50f
            binding.root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((position * 80).toLong())
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    class CatDiffCallback : DiffUtil.ItemCallback<Cat>() {
        override fun areItemsTheSame(oldItem: Cat, newItem: Cat) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cat, newItem: Cat) = oldItem == newItem
    }
}
