package com.example.incash_proyecto.db

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaveTest {

    @Test
    fun mismaClaveYSaltDanElMismoHash() {
        val salt = ByteArray(16) { it.toByte() }
        assertTrue(clavesIguales(hashClave("12345678", salt), hashClave("12345678", salt)))
    }

    @Test
    fun claveDistintaNoCoincide() {
        val salt = ByteArray(16) { it.toByte() }
        assertFalse(clavesIguales(hashClave("12345678", salt), hashClave("12345679", salt)))
    }

    @Test
    fun saltDistintoCambiaElHash() {
        assertNotEquals(hashClave("12345678", ByteArray(16)), hashClave("12345678", ByteArray(16) { 1 }))
    }
}
