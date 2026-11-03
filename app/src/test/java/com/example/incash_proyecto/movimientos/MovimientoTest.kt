package com.example.incash_proyecto.movimientos

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MovimientoTest {

    @Test
    fun formatoCopAgrupaConPuntos() {
        assertEquals("$1.500.000", formatoCop(1_500_000))
        assertEquals("$650.000", formatoCop(650_000))
        assertEquals("$0", formatoCop(0))
    }

    @Test
    fun etiquetaFechaHoyAyerYOtra() {
        val hoy = LocalDate.of(2026, 11, 3)
        assertEquals("Hoy", etiquetaFecha(hoy, hoy))
        assertEquals("Ayer", etiquetaFecha(hoy.minusDays(1), hoy))
        assertEquals("1 de noviembre de 2026", etiquetaFecha(hoy.minusDays(2), hoy))
    }
}
