package com.basicusapps.smartalarm.ui.missions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basicusapps.smartalarm.databinding.ItemMemoryCardBinding

class MemoryCardAdapter(
    private val onCardClick: (Int) -> Unit
) : ListAdapter<MemoryCard, MemoryCardAdapter.ViewHolder>(MemoryCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMemoryCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onCardClick(bindingAdapterPosition)
                }
            }
        }

        fun bind(card: MemoryCard) {
            binding.apply {
                cardCover.alpha = if (card.isFaceUp || card.isMatched) 0f else 1f
                cardSymbol.text = card.symbol.toString()
                root.isClickable = !card.isMatched
            }
        }
    }
}

private class MemoryCardDiffCallback : DiffUtil.ItemCallback<MemoryCard>() {
    override fun areItemsTheSame(oldItem: MemoryCard, newItem: MemoryCard): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MemoryCard, newItem: MemoryCard): Boolean {
        return oldItem == newItem
    }
} 