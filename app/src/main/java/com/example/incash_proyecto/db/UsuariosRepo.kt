package com.example.incash_proyecto.db

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Usuario(val id: Long, val nombre: String)

object UsuariosRepo {

    /** Crea el usuario. Devuelve null si el correo ya está registrado. */
    suspend fun registrar(context: Context, nombre: String, email: String, clave: String): Usuario? =
        withContext(Dispatchers.IO) {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val valores = ContentValues().apply {
                put("nombre", nombre)
                put("email", email.lowercase())
                put("pass_hash", hashClave(clave, salt))
                put("salt", Base64.getEncoder().encodeToString(salt))
            }
            // insert devuelve -1 si choca con el unique del correo.
            val id = AppDb.get(context).writableDatabase.insert("usuarios", null, valores)
            if (id == -1L) null else Usuario(id, nombre)
        }

    /** Devuelve el usuario si el correo y la contraseña coinciden; si no, null. */
    suspend fun login(context: Context, email: String, clave: String): Usuario? =
        withContext(Dispatchers.IO) {
            AppDb.get(context).readableDatabase.rawQuery(
                "select id, nombre, pass_hash, salt from usuarios where email = ?",
                arrayOf(email.lowercase())
            ).use { c ->
                if (!c.moveToFirst()) return@use null
                val salt = Base64.getDecoder().decode(c.getString(3))
                if (clavesIguales(hashClave(clave, salt), c.getString(2))) Usuario(c.getLong(0), c.getString(1)) else null
            }
        }
}

/** PBKDF2: la contraseña nunca se guarda en texto plano. */
fun hashClave(clave: String, salt: ByteArray): String {
    val spec = PBEKeySpec(clave.toCharArray(), salt, 10_000, 256)
    val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    return Base64.getEncoder().encodeToString(hash)
}

fun clavesIguales(a: String, b: String) = MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
