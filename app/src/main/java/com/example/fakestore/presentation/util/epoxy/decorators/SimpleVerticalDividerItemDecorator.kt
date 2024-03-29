package com.example.fakestore.presentation.util.epoxy.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SimpleVerticalDividerItemDecorator(private val spacingDp: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val viewHolder = parent.getChildViewHolder(view)
        val currentPosition =
            parent.getChildAdapterPosition(view).takeIf { it != RecyclerView.NO_POSITION }
                ?: viewHolder.oldPosition

        val oneSideVerticalDivider = spacingDp / 2
        with(outRect) {
            if (currentPosition != state.itemCount - 1) {
                top = oneSideVerticalDivider
                bottom = oneSideVerticalDivider
            } else {
                outRect.setEmpty()
            }
        }
    }
}
