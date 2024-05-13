package com.plcoding.bma.util

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.plcoding.bma.R

fun Fragment.snackbar(
    @StringRes text: Int,
    anchor: View? = null,
    view: View? = null
) {
    Snackbar.make(
        view ?: requireView(),
        text,
        Snackbar.LENGTH_LONG
    ).apply {
        if(anchor != null) {
            anchorView = anchor
        }
    }.show()
}