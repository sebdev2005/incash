package com.example.incash_proyecto

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.incash_proyecto.db.Usuario

/** Sesión del usuario guardada en SharedPreferences: se recuerda al cerrar la app. */
object Sesion {
    private fun prefs(context: Context) = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun iniciar(context: Context, usuario: Usuario) {
        prefs(context).edit().putLong("usuario_id", usuario.id).putString("nombre", usuario.nombre).apply()
    }

    /** -1 si no hay sesión. */
    fun usuarioId(context: Context): Long = prefs(context).getLong("usuario_id", -1)

    fun nombre(context: Context): String = prefs(context).getString("nombre", "") ?: ""

    /** Borra la sesión y vuelve al login sin dejar pantallas atrás. */
    fun cerrar(activity: Activity) {
        prefs(activity).edit().clear().apply()
        activity.startActivity(
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
