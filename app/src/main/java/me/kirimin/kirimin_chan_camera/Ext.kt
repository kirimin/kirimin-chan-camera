package me.kirimin.kirimin_chan_camera

import android.content.Context

fun Int.toDp(context: Context) = this / context.resources.displayMetrics.density