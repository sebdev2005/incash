package com.example.incash_proyecto.movimientos

import android.content.ContentValues
import android.content.Context
import com.example.incash_proyecto.Sesion
import com.example.incash_proyecto.db.AppDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Movimientos del usuario con sesión iniciada, en la base local. */
object MovimientosRepo {

    /** tipo = null trae todos; limite = null trae todos. */
    suspend fun listar(context: Context, tipo: String?, limite: Int?): List<Movimiento> = withContext(Dispatchers.IO) {
        val args = mutableListOf(Sesion.usuarioId(context).toString())
        var where = "usuario_id = ?"
        if (tipo != null) {
            where += " and tipo = ?"
            args += tipo
        }
        AppDb.get(context).readableDatabase.rawQuery(
            "select id, tipo, monto, categoria, metodo_pago, descripcion, fecha from movimientos " +
                "where $where order by fecha desc, id desc limit ${limite ?: -1}",
            args.toTypedArray()
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(
                    Movimiento(
                        c.getLong(0), c.getString(1), c.getLong(2), c.getString(3),
                        c.getString(4), c.getString(5), LocalDate.parse(c.getString(6))
                    )
                )
            }
        }
    }

    /** (ingresos, gastos) desde el día indicado. */
    suspend fun totalesDesde(context: Context, desde: LocalDate): Pair<Long, Long> = withContext(Dispatchers.IO) {
        AppDb.get(context).readableDatabase.rawQuery(
            "select coalesce(sum(case when tipo = 'ingreso' then monto end), 0), " +
                "coalesce(sum(case when tipo = 'gasto' then monto end), 0) " +
                "from movimientos where usuario_id = ? and fecha >= ?",
            arrayOf(Sesion.usuarioId(context).toString(), desde.toString())
        ).use { c ->
            c.moveToFirst()
            c.getLong(0) to c.getLong(1)
        }
    }

    /** Inserta si id es null; si no, actualiza. */
    suspend fun guardar(context: Context, m: Movimiento) = withContext(Dispatchers.IO) {
        val usuarioId = Sesion.usuarioId(context)
        val valores = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("tipo", m.tipo)
            put("monto", m.monto)
            put("categoria", m.categoria)
            put("metodo_pago", m.metodoPago)
            put("descripcion", m.descripcion)
            put("fecha", m.fecha.toString())
        }
        val db = AppDb.get(context).writableDatabase
        if (m.id == null) db.insertOrThrow("movimientos", null, valores)
        else db.update("movimientos", valores, "id = ? and usuario_id = ?", arrayOf(m.id.toString(), usuarioId.toString()))
    }

    suspend fun eliminar(context: Context, id: Long) = withContext(Dispatchers.IO) {
        AppDb.get(context).writableDatabase.delete(
            "movimientos", "id = ? and usuario_id = ?", arrayOf(id.toString(), Sesion.usuarioId(context).toString())
        )
    }
}
