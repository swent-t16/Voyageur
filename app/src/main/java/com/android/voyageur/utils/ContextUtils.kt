package com.android.voyageur.utils

import android.content.Context
import android.content.ContextWrapper
import com.android.voyageur.MainActivity

/**
 * Finds the main activity from a context. Can be used to know if you are in test mode or not
 *
 * @return The main activity if found, or null if not found.
 */
fun Context.findMainActivityOrNull(): MainActivity? {
  var context = this
  while (context is ContextWrapper) {
    if (context is MainActivity) return context
    context = context.baseContext
  }
  return null
}
