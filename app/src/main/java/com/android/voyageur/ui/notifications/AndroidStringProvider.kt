package com.android.voyageur.ui.notifications

import android.content.Context

/**
 * AndroidStringProvider is responsible for providing string resources.
 *
 * @param context The context used to access resources.
 */
class AndroidStringProvider(private val context: Context) : StringProvider {

  /**
   * Returns the string associated with the specified resource ID.
   *
   * @param resId The resource ID of the string.
   * @return The string associated with the specified resource ID.
   */
  override fun getString(resId: Int): String = context.getString(resId)
}