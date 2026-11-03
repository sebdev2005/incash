package com.example.incash_proyecto.movimientos

import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Movimiento(
    val id: Long?,
    val tipo: String, // "ingreso" o "gasto"
    val monto: Long,
    val categoria: String,
    val metodoPago: String?,
    val descripcion: String,
    val fecha: LocalDate
) : Serializable {
    val esIngreso get() = tipo == "ingreso"
}

val CATEGORIAS = arrayOf("Alimentación", "Transporte", "Entretenimiento", "Compras", "Otros", "Salario")
val METODOS_PAGO = arrayOf("Efectivo", "Tarjeta", "Transferencia")

// Locale.GERMANY agrupa miles con punto, como el COP: 1500000 -> $1.500.000
fun formatoCop(monto: Long): String = "$" + String.format(Locale.GERMANY, "%,d", monto)

private val formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))

fun etiquetaFecha(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String = when (fecha) {
    hoy -> "Hoy"
    hoy.minusDays(1) -> "Ayer"
    else -> fecha.format(formatoFecha)
}
