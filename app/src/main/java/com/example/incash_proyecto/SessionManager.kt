package com.example.incash_proyecto

import android.content.Context
import androidx.core.content.edit

/** Sesión local mínima (SharedPreferences). Cuando haya login real se reemplaza por token/id de usuario. */
object SessionManager {
    private const val PREFS = "incash_session"
    private const val KEY_EMAIL = "email"

    private fun prefs(ctx: Context) = ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun guardarSesion(ctx: Context, email: String) = prefs(ctx).edit { putString(KEY_EMAIL, email) }

    fun email(ctx: Context): String? = prefs(ctx).getString(KEY_EMAIL, null)

    fun haySesion(ctx: Context) = email(ctx) != null

    fun cerrarSesion(ctx: Context) = prefs(ctx).edit { clear() }
}
