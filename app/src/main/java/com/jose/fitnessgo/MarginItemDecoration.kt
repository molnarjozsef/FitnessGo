package com.jose.fitnessgo

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceHeight
            }
            left =  0
            right = 0
            bottom = 0
        }
    }
}