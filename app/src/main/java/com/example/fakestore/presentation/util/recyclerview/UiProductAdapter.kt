package com.example.fakestore.presentation.util.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.example.fakestore.presentation.util.epoxy.listeners.GeneralProductClickListener
import com.example.fakestore.presentation.model.UiProduct

private val callback = object : ItemCallback<UiProduct>() {

    override fun areItemsTheSame(oldItem: UiProduct, newItem: UiProduct): Boolean {
        return oldItem.product.id == newItem.product.id
    }

    override fun areContentsTheSame(oldItem: UiProduct, newItem: UiProduct): Boolean {
        return oldItem == newItem
    }
}

class UiProductAdapter : ListAdapter<UiProduct, UiProductViewHolder>(callback) {
    var btnListener: GeneralProductClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UiProductViewHolder {
        return UiProductViewHolder.create(parent, btnListener)
    }

    override fun onBindViewHolder(holder: UiProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
