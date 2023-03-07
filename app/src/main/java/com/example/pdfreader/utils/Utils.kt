package com.example.pdfreader.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(
    msg: String,
) {
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()
}