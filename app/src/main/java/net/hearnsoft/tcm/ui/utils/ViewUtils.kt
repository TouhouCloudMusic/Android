package net.hearnsoft.tcm.ui.utils

import android.content.Context
import android.util.TypedValue

class ViewUtils {
    fun spToDp(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}