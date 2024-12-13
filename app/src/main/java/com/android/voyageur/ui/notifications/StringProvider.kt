package com.android.voyageur.ui.notifications

/**
 * StringProvider is an interface for providing string resources.
 */
interface StringProvider {

  /**
   * Returns the string associated with the specified resource ID.
   *
   * @param resId The resource ID of the string.
   * @return The string associated with the specified resource ID.
   */
  fun getString(resId: Int): String
}