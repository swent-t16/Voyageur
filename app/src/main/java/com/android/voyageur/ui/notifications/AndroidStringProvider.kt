package com.android.voyageur.ui.notifications

import android.content.Context

class AndroidStringProvider(private val context: Context) : StringProvider {
  override fun getString(resId: Int): String = context.getString(resId)
}
